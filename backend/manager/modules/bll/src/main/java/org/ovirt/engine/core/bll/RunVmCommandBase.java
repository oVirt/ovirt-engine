package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.config.ConfigValues.VdsRefreshRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FailedToRunVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsMonitor;

/**
 * Base class for asynchronous running process handling
 */
public abstract class RunVmCommandBase<T extends VmOperationParameterBase> extends VmCommand<T> implements
        IVdsAsyncCommand, RunVmDelayer {

    private static Log log = LogFactory.getLog(RunVmCommandBase.class);
    protected static final HashMap<Guid, Integer> _vds_pending_vm_count = new HashMap<Guid, Integer>();
    private VdsSelector privateVdsSelector;
    protected boolean _isRerun = false;
    protected VDS _destinationVds;
    private SnapshotsValidator snapshotsValidator=new SnapshotsValidator();

    protected RunVmCommandBase(Guid commandId) {
        super(commandId);
    }

    public RunVmCommandBase(T parameters) {
        super(parameters);
    }

    protected abstract VDS getDestinationVds();

    protected VdsSelector getVdsSelector() {
        return privateVdsSelector;
    }

    protected void setVdsSelector(VdsSelector value) {
        privateVdsSelector = value;
    }

    public SnapshotsValidator getSnapshotsValidator() {
        return snapshotsValidator;
    }

    /**
     * List on all vdss, vm run on. In the case of problem to run vm will be more then one
     */
    private List<Guid> getRunVdssList() {
        return getVdsSelector().getRunVdssList();
    }

    /**
     * Check if the given host has enough CPU to run the VM, without exceeding the high utilization threshold.
     *
     * @param vds
     *            The host to check.
     * @param vm
     *            The VM to check.
     * @return Does this host has enough CPU (without exceeding the threshold) to run the VM.
     */
    public static boolean hasCpuToRunVM(VDS vds, VM vm) {
        if (vds.getUsageCpuPercent() == null || vm.getUsageCpuPercent() == null) {
            return false;
        }

        // The predicted CPU is actually the CPU that the VM will take considering how many cores it has and now many
        // cores the host has. This is why we take both parameters into consideration.
        int predictedVmCpu = (vm.getUsageCpuPercent() * vm.getNumOfCpus()) / VdsSelector.getEffectiveCpuCores(vds);
        boolean result = vds.getUsageCpuPercent() + predictedVmCpu <= vds.getHighUtilization();
        if (log.isDebugEnabled()) {
            log.debugFormat("Host {0} has {1}% CPU load; VM {2} is predicted to have {3}% CPU load; " +
                    "High threshold is {4}%. Host is {5}suitable in terms of CPU.",
                    vds.getName(),
                    vds.getUsageCpuPercent(),
                    vm.getName(),
                    predictedVmCpu,
                    vds.getHighUtilization(),
                    (result ? "" : "not "));
        }

        return result;
    }

    public static boolean hasMemoryToRunVM(VDS curVds, VM vm) {
        boolean retVal = false;
        if (curVds.getMemCommited() != null && curVds.getPhysicalMemMb() != null && curVds.getReservedMem() != null) {
            double vdsCurrentMem =
                    curVds.getMemCommited() + curVds.getPendingVmemSize() + curVds.getGuestOverhead() + curVds
                            .getReservedMem() + vm.getMinAllocatedMem();
            double vdsMemLimit = curVds.getMaxVdsMemoryOverCommit() * curVds.getPhysicalMemMb() / 100.0;
            if (log.isDebugEnabled()) {
                log.debugFormat("hasMemoryToRunVM: host {0} pending vmem size is : {1} MB",
                        curVds.getName(),
                        curVds.getPendingVmemSize());
                log.debugFormat("Host Mem Conmmitted: {0}, Host Reserved Mem: {1}, Host Guest Overhead {2}, VM Min Allocated Mem {3}",
                        curVds.getMemCommited(),
                        curVds.getReservedMem(),
                        curVds.getGuestOverhead(),
                        vm.getMinAllocatedMem());
                log.debugFormat("{0} <= ???  {1}", vdsCurrentMem, vdsMemLimit);
            }
            retVal = (vdsCurrentMem <= vdsMemLimit);
        }
        return retVal;
    }

    @Override
    public void rerun() {
        Guid vdsId = getDestinationVds() != null ? getDestinationVds().getId() : getCurrentVdsId();
        decreasePendingVms(vdsId);

        setSucceeded(false);
        setVm(null);

        // by default, if rerun is called then rerun process is about to start so log the result of the
        //previous run as if rerun is about to begin (and change it later in case rerun isn't going to happen)
        _isRerun = true;
        log();

        /**
         * Rerun VM only if not exceeded maximum rerun attempts. for example if there are 10 hosts that can run VM and
         * predefine maximum 3 attempts to rerun VM - on 4th turn vm will stop to run despite there are still available
         * hosts to run it DO NOT TRY TO RERUN IF RESUME FAILED.
         */
        if (getRunVdssList().size() < Config.<Integer> GetValue(ConfigValues.MaxRerunVmOnVdsCount)
                && getVm().getStatus() != VMStatus.Paused) {
            // restore CanDoAction value to false so CanDoAction checks will run again
            getReturnValue().setCanDoAction(false);
            if (getExecutionContext() != null) {
                Job job = getExecutionContext().getJob();
                if (job != null) {
                    // mark previous steps as fail
                    JobRepositoryFactory.getJobRepository().closeCompletedJobSteps(job.getId(), JobExecutionStatus.FAILED);
                }
            }
            // set the _isRerun flag to false before calling executeAction so that we'll know if
            // there is another rerun attempt within the method
            _isRerun = false;
            executeAction();

            // if there was no rerun attempt in the previous executeAction call and the command
            // wasn't done because canDoAction check returned false..
            if (!_isRerun && !getReturnValue().getCanDoAction()) {
                log();
                failedToRunVm();
            }

            // signal the caller that a rerun was made
            _isRerun = true;
        } else {
            Backend.getInstance().getResourceManager().RemoveAsyncRunningCommand(getVmId());
            failedToRunVm();
            _isRerun = false;
            log();
        }
    }

    protected void failedToRunVm() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                processVmPoolOnStopVm();
            }
        });
        ExecutionHandler.setAsyncJob(getExecutionContext(), false);
        ExecutionHandler.endJob(getExecutionContext(), false);
    }

    private void processVmPoolOnStopVm() {
        VmPoolHandler.ProcessVmPoolOnStopVm(getVm().getId(),
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
    }

    /**
     * Asynchronous event, send by vds on running vm success. Vm decided successfully run when it's status turn to Up.
     * If there are vdss, not succeeded to run vm - treat them as suspicious.
     */
    @Override
    public void runningSucceded() {
        setSucceeded(true);
        setActionReturnValue(VMStatus.Up);
        log();
        ExecutionHandler.setAsyncJob(getExecutionContext(), false);
        ExecutionHandler.endJob(getExecutionContext(), true);
        notifyHostsVmFailed();

        if (getVm().getLastVdsRunOn() == null || !getVm().getLastVdsRunOn().equals(getCurrentVdsId())) {
            getVm().setLastVdsRunOn(getCurrentVdsId());
        }
        if (StringUtils.isNotEmpty(getVm().getHibernationVolHandle())) {
            handleHibernatedVm(getActionType(), true);
            // In order to prevent a race where VdsUpdateRuntimeInfo saves the Vm Dynamic as UP prior to execution of
            // this method (which is a part of the cached VM command,
            // so the state this method is aware to is RESTORING, in case of RunVmCommand after the VM got suspended.
            // In addition, as the boolean return value of HandleHIbernateVm is ignored here, it is safe to set the
            // status to up.
            getVm().setStatus(VMStatus.Up);
            getVm().setHibernationVolHandle(null);
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getCurrentVdsId(), getVm().getDynamicData()));
        }
    }

    /**
     * notify other hosts on a failed attempt to run a Vm in a non blocking matter
     * to avoid deadlock where other host's VdsManagers lock is taken and is awaiting the current vds lock.
     */
    private void notifyHostsVmFailed() {
        if (!getRunVdssList().isEmpty()) {
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    for (Guid vdsId : getRunVdssList()) {
                        if (!getCurrentVdsId().equals(vdsId)) {
                            Backend.getInstance().getResourceManager()
                                    .RunVdsCommand(VDSCommandType.FailedToRunVm, new FailedToRunVmVDSCommandParameters(vdsId));
                        }
                    }
                }

            });
        }
    }

    @Override
    public void reportCompleted() {
        ExecutionContext executionContext = getExecutionContext();
        if (executionContext != null && executionContext.isMonitored()) {
            if (!executionContext.isCompleted()) {
                if (executionContext.getExecutionMethod() == ExecutionMethod.AsJob) {
                    ExecutionHandler.endJob(executionContext, false);
                } else if (executionContext.getExecutionMethod() == ExecutionMethod.AsStep) {
                    ExecutionHandler.endStep(executionContext, executionContext.getStep(), false);
                }
            }
        }
    }

    @Override
    protected void endVmCommand() {
        setCommandShouldBeLogged(false);
        setSucceeded(true);
    }

    protected Guid getCurrentVdsId() {
        return getVds().getId();
    }

    @Override
    public boolean getAutoStart() {
        return getVm().isAutoStartup();
    }

    @Override
    public Guid getAutoStartVdsId() {
        return null;
    }

    protected boolean connectLunDisks(Guid hostId) {
        if (getVm().getDiskMap().isEmpty()) {
            VmHandler.updateDisksFromDb(getVm());
        }
        List<LunDisk> lunDisks = ImagesHandler.filterDiskBasedOnLuns(getVm().getDiskMap().values());
        for (LunDisk lunDisk : lunDisks) {
            LUNs lun = lunDisk.getLun();
            lun.setLunConnections(new ArrayList<StorageServerConnections>(DbFacade.getInstance()
                                            .getStorageServerConnectionDao()
                                            .getAllForLun(lun.getLUN_id())));

            if (!lun.getLunConnections().isEmpty()
                    && !StorageHelperDirector.getInstance().getItem(lun.getLunConnections().get(0).getstorage_type())
                            .connectStorageToLunByVdsId(null, hostId, lun, getVm().getStoragePoolId())) {
                log.infoFormat("Failed to connect  a lun disk to vdsm {0} skiping it", hostId);
                return false;
            }

        }
        return true;
    }

    protected void decreasePendingVms(Guid vdsId) {
        DbFacade.getInstance()
                .getVdsDynamicDao()
                .updatePartialVdsDynamicCalc(vdsId, 0, -getVm().getNumOfCpus(), -getVm().getMinAllocatedMem(), 0, 0);
        getBlockingQueue(vdsId).offer(Boolean.TRUE);

        if (log.isDebugEnabled()) {
            log.debugFormat("DecreasePendingVms::Decreasing vds {0} pending vcpu count, in {1}. Vm: {2}",
                    vdsId, getVm().getNumOfCpus(), getVm().getName());
            log.debugFormat("DecreasePendingVms::Decreasing vds {0} pending vmem size, in {1}. Vm: {2}",
                    vdsId, getVm().getMinAllocatedMem(), getVm().getName());
        }
    }

    /**
     * throttle bulk run of VMs by waiting for the update of run-time to kick in and fire <br>
     * the DecreasePendingVms event.
     * @see VdsEventListener
     * @See VdsUpdateRunTimeInfo
     */
    @Override
    public void delay(Guid vdsId) {
        log.debug("try to wait for te engine update the host memory and cpu stats");

        try {
            // time out waiting for an update is the highest between the refresh rate and the last update elapsed time
            // but still no higher than a configurable max to prevent very long updates to stall command.
            long t = Math.max(
                    ResourceManager.getInstance().GetVdsManager(vdsId).getLastUpdateElapsed(),
                    TimeUnit.SECONDS.toMillis(Config.<Integer> GetValue(VdsRefreshRate)));
            t = Math.max(Config.<Integer> GetValue(ConfigValues.ThrottlerMaxWaitForVdsUpdateInMillis), t);

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
     * @param vdsId
     * @return {@link org.ovirt.engine.core.vdsbroker.VdsMonitor} for signaling on thread actions
     */
    private VdsMonitor getMonitor(Guid vdsId) {
        return ResourceManager.getInstance().GetVdsManager(vdsId).getVdsMonitor();
    }

    @Override
    public void onPowerringUp() {
        decreasePendingVms(getCurrentVdsId());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
