package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.config.ConfigValues.VdsRefreshRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.scheduling.RunVmDelayer;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.storage.disk.cinder.CinderBroker;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FailedToRunVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.base.client.OpenStackResponseException;

/**
 * Base class for asynchronous running process handling
 */
public abstract class RunVmCommandBase<T extends VmOperationParameterBase> extends VmCommand<T> implements
        IVdsAsyncCommand, RunVmDelayer {

    private static final Logger log = LoggerFactory.getLogger(RunVmCommandBase.class);
    protected boolean _isRerun;
    private SnapshotsValidator snapshotsValidator=new SnapshotsValidator();
    private final List<Guid> runVdsList = new ArrayList<>();
    @Inject
    protected SchedulingManager schedulingManager;

    @Inject
    private ResourceManager resourceManager;

    protected RunVmCommandBase(Guid commandId) {
        super(commandId);
    }

    public RunVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    public SnapshotsValidator getSnapshotsValidator() {
        return snapshotsValidator;
    }

    /**
     * List on all VDSs, vm run on. In the case of problem to run vm will be more than one
     */
    protected List<Guid> getRunVdssList() {
        return runVdsList;
    }

    @Override
    public void rerun() {
        decreasePendingVm();

        setSucceeded(false);
        setVm(null);

        // by default, if rerun is called then rerun process is about to start so log the result of the
        //previous run as if rerun is about to begin (and change it later in case rerun isn't going to happen)
        _isRerun = true;
        log();

        // set _isRerun flag to false so that we'll be able to know if
        // there is another rerun attempt within the method
        _isRerun = false;

        /**
         * Rerun VM only if not exceeded maximum rerun attempts. for example if there are 10 hosts that can run VM and
         * predefine maximum 3 attempts to rerun VM - on 4th turn vm will stop to run despite there are still available
         * hosts to run it DO NOT TRY TO RERUN IF RESUME FAILED.
         */
        if (getRunVdssList().size() < Config.<Integer> getValue(ConfigValues.MaxRerunVmOnVdsCount)
                && getVm().getStatus() != VMStatus.Paused) {
            reexecuteCommand();

            // if there was no rerun attempt in the previous executeAction call and the command
            // wasn't done because validate check returned false..
            if (!_isRerun && !getReturnValue().isValid()) {
                runningFailed();
            }

            // signal the caller that a rerun was made
            _isRerun = true;
        } else {
            runningFailed();
        }
    }

    protected void reexecuteCommand() {
        // restore Validate value to false so Validate checks will run again
        getReturnValue().setValid(false);
        if (getExecutionContext() != null) {
            Job job = getExecutionContext().getJob();
            if (job != null) {
                // mark previous steps as fail
                JobRepositoryFactory.getJobRepository().closeCompletedJobSteps(job.getId(), JobExecutionStatus.FAILED);
            }
        }
        executeAction();
    }

    protected void runningFailed() {
        try {
            decreasePendingVm();
            getVdsBroker().removeAsyncRunningCommand(getVmId());
            setCommandShouldBeLogged(false);
            _isRerun = false;
            setSucceeded(false);
            log();
            processVmOnDown();
            ExecutionHandler.setAsyncJob(getExecutionContext(), false);
            ExecutionHandler.endJob(getExecutionContext(), false);
        }
        finally {
            freeLock();
        }
    }

    protected void processVmOnDown() {
        ThreadPoolUtil.execute(() -> runInternalActionWithTasksContext(
                VdcActionType.ProcessDownVm,
                new ProcessDownVmParameters(getVm().getId())
        ));
    }

    /**
     * Asynchronous event, send by vds on running vm success. Vm decided successfully run when it's status turn to Up.
     * If there are vdss, not succeeded to run vm - treat them as suspicious.
     */
    @Override
    public void runningSucceded() {
        try {
            decreasePendingVm();
            setSucceeded(true);
            setActionReturnValue(VMStatus.Up);
            log();
            ExecutionHandler.setAsyncJob(getExecutionContext(), false);
            ExecutionHandler.endJob(getExecutionContext(), true);
            notifyHostsVmFailed();

            if (getVm().getLastVdsRunOn() == null || !getVm().getLastVdsRunOn().equals(getCurrentVdsId())) {
                getVm().setLastVdsRunOn(getCurrentVdsId());
            }
        }
        finally {
            freeLock();
        }
    }

    /**
     * notify other hosts on a failed attempt to run a Vm in a non blocking matter
     * to avoid deadlock where other host's VdsManagers lock is taken and is awaiting the current vds lock.
     */
    private void notifyHostsVmFailed() {
        if (!getRunVdssList().isEmpty()) {
            ThreadPoolUtil.execute(() -> {
                for (Guid vdsId : getRunVdssList()) {
                    if (!vdsId.equals(getCurrentVdsId())) {
                        runVdsCommand(VDSCommandType.FailedToRunVm, new FailedToRunVmVDSCommandParameters(vdsId));
                    }
                }
            });
        }
    }

    @Override
    public final void reportCompleted() {
        try {
            // decrease pending resources if they were not decreased already
            decreasePendingVm();
            // end the execution job if needed
            ExecutionContext executionContext = getExecutionContext();
            if (executionContext != null && executionContext.isMonitored()
                    && !executionContext.isCompleted()) {
                endExecutionMonitoring();
            }
        }
        finally {
            freeLock();
        }
    }

    protected void endExecutionMonitoring() {
        ExecutionContext executionContext = getExecutionContext();
        switch (executionContext.getExecutionMethod()) {
        case AsJob:
            ExecutionHandler.endJob(executionContext, false);
            break;
        case AsStep:
            ExecutionHandler.endStep(executionContext, executionContext.getStep(), false);
            break;
        default:
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);
        setSucceeded(true);
    }

    protected Guid getCurrentVdsId() {
        VDS vds = getVds();
        return vds != null ? vds.getId() : null;
    }

    protected boolean connectLunDisks(Guid hostId) {
        if (getVm().getDiskMap().isEmpty()) {
            VmHandler.updateDisksFromDb(getVm());
        }
        List<LunDisk> lunDisks = ImagesHandler.filterDiskBasedOnLuns(getVm().getDiskMap().values(), true);
        for (LunDisk lunDisk : lunDisks) {
            LUNs lun = lunDisk.getLun();
            lun.setLunConnections(new ArrayList<>(DbFacade.getInstance()
                    .getStorageServerConnectionDao()
                    .getAllForLun(lun.getLUNId())));

            if (!lun.getLunConnections().isEmpty()
                    && !StorageHelperDirector.getInstance().getItem(lun.getLunConnections().get(0).getStorageType())
                            .connectStorageToLunByVdsId(null, hostId, lun, getVm().getStoragePoolId())) {
                log.info("Failed to connect  a lun disk to vdsm '{}' skiping it", hostId);
                return false;
            }

        }
        return true;
    }

    protected boolean updateCinderDisksConnections() {
        if (getVm().getDiskMap().isEmpty()) {
            VmHandler.updateDisksFromDb(getVm());
        }
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(getVm().getDiskMap().values(), true);
        for (CinderDisk cinderDisk : cinderDisks) {
            CinderBroker cinderBroker = new CinderBroker(cinderDisk.getStorageIds().get(0), getReturnValue().getExecuteFailedMessages());
            try {
                cinderBroker.updateConnectionInfoForDisk(cinderDisk);
            } catch (OpenStackResponseException ex) {
                log.info("Update cinder disk connection failure", ex);
                return false;
            }
        }
        return true;
    }

    private void decreasePendingVm() {
        decreasePendingVm(getVm().getStaticData());
    }

    protected final void decreasePendingVm(VmStatic vm) {
        Guid vdsId = getCurrentVdsId();
        schedulingManager.clearPendingVm(vm);
        if (vdsId != null) {
            getBlockingQueue(vdsId).offer(Boolean.TRUE);
        }
    }

    /**
     * throttle bulk run of VMs by waiting for the update of run-time to kick in and fire <br>
     * the DecreasePendingVms event.
     * @see VdsEventListener
     * @see HostMonitoring
     */
    @Override
    public void delay(Guid vdsId) {
        log.debug("Try to wait for te engine update the host memory and cpu stats");

        try {
            // time out waiting for an update is the highest between the refresh rate and the last update elapsed time
            // but still no higher than a configurable max to prevent very long updates to stall command.
            long t = Math.max(
                    resourceManager.getVdsManager(vdsId).getLastUpdateElapsed(),
                    TimeUnit.SECONDS.toMillis(Config.<Integer> getValue(VdsRefreshRate)));
            t = Math.max(Config.<Integer> getValue(ConfigValues.ThrottlerMaxWaitForVdsUpdateInMillis), t);

            // wait for the run-time refresh to decrease any current powering-up VMs
            getBlockingQueue(vdsId).poll(t, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private BlockingQueue<Boolean> getBlockingQueue(Guid vdsId) {
        return getMonitor(vdsId).getQueue();
    }

    /**
     * get the monitor object of this host. VDSs have monitors exposed by their {@link org.ovirt.engine.core.vdsbroker.VdsManager}
     *
     * @return VdsMonitor for signaling on thread actions
     */
    private VdsMonitor getMonitor(Guid vdsId) {
        return resourceManager.getVdsManager(vdsId).getVdsMonitor();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getExclusiveLocksForRunVm(getVmId(), getLockMessage());
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return getSharedLocksForRunVm();
    }

    protected static Map<String, Pair<String, String>> getExclusiveLocksForRunVm(Guid vmId, String lockMessage) {
        return Collections.singletonMap(
                vmId.toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VM,
                        lockMessage));
    }

    protected static Map<String, Pair<String, String>> getSharedLocksForRunVm() {
        return null;
    }

    /**
     * Returns a message that explains what this command does. The message is
     * shown when other command is blocked because it conflicts with this
     * command, so the user should understand what cause it the conflict.
     *
     * @return String explaining what this command does
     */
    protected String getLockMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }
}
