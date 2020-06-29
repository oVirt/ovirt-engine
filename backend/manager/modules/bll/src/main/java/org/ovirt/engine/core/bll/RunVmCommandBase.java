package org.ovirt.engine.core.bll;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.disk.cinder.CinderBroker;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.provider.HostProviderBindingDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
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
        IVdsAsyncCommand {

    private static final Logger log = LoggerFactory.getLogger(RunVmCommandBase.class);
    protected boolean _isRerun;
    private final List<Guid> runVdsList = new ArrayList<>();

    @Inject
    protected VfScheduler vfScheduler;
    @Inject
    protected NetworkDeviceHelper networkDeviceHelper;
    @Inject
    protected SchedulingManager schedulingManager;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    protected JobRepository jobRepository;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private NetworkHelper networkHelper;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private HostProviderBindingDao hostProviderBindingDao;
    @Inject
    private VmDao vmDao;

    protected RunVmCommandBase(Guid commandId) {
        super(commandId);
    }

    public RunVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected final boolean validate() {
        final boolean result = validateImpl();
        if (!result && !isInternalExecution() && !getParameters().isRerun()) {
            logValidationFailed();
        }
        return result;
    }

    protected abstract boolean validateImpl();
    protected abstract void logValidationFailed();

    /**
     * List on all VDSs, vm run on. In the case of problem to run vm will be more than one
     */
    protected List<Guid> getRunVdssList() {
        return runVdsList;
    }

    protected void cleanupPassthroughVnics(Guid hostId) {
        Map<Guid, String> vnicToVfMap = getVnicToVfMap(hostId);
        if (vnicToVfMap != null) {
            networkDeviceHelper.setVmIdOnVfs(hostId, null, new HashSet<>(vnicToVfMap.values()));
        }

        vfScheduler.cleanVmData(getVmId());
    }

    protected Map<Guid, String> getVnicToVfMap(Guid hostId) {
        return hostId == null ? null : vfScheduler.getVnicToVfMap(getVmId(), hostId);
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
        getParameters().setRerun(true);
        if (getExecutionContext() != null) {
            Job job = getExecutionContext().getJob();
            if (job != null) {
                // mark previous steps as fail
                jobRepository.closeCompletedJobSteps(job.getId(), JobExecutionStatus.FAILED);
            }
        }
        // Re-initiate the VM configuration in order to persist it on re-run attempts.
        init();
        executeAction();
    }

    protected void runningFailed() {
        try {
            decreasePendingVm();
            vdsBroker.removeAsyncRunningCommand(getVmId());
            setCommandShouldBeLogged(false);
            _isRerun = false;
            setSucceeded(false);
            log();
            processVmOnDown();
            ExecutionHandler.setAsyncJob(getExecutionContext(), false);
            executionHandler.endJob(getExecutionContext(), false);
        } finally {
            freeLock();
        }
    }

    protected void processVmOnDown() {
        ThreadPoolUtil.execute(() -> runInternalActionWithTasksContext(
                ActionType.ProcessDownVm,
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
            VM vm = getVm();
            if (!vm.isInitialized()) {
                vmDao.saveIsInitialized(vm.getId(), true);
            }
            vm.setInitialized(true);
            decreasePendingVm();
            setSucceeded(true);
            setActionReturnValue(VMStatus.Up);
            log();
            ExecutionHandler.setAsyncJob(getExecutionContext(), false);
            executionHandler.endJob(getExecutionContext(), true);
        } finally {
            freeLock();
        }
    }

    @Override
    public void reportCompleted() {
        try {
            // decrease pending resources if they were not decreased already
            decreasePendingVm();
            // end the execution job if needed
            ExecutionContext executionContext = getExecutionContext();
            if (executionContext != null && executionContext.isMonitored()
                    && !executionContext.isCompleted()) {
                endExecutionMonitoring();
            }
        } finally {
            freeLock();
        }
    }

    protected void endExecutionMonitoring() {
        ExecutionContext executionContext = getExecutionContext();
        switch (executionContext.getExecutionMethod()) {
        case AsJob:
            executionHandler.endJob(executionContext, false);
            break;
        case AsStep:
            executionHandler.endStep(executionContext, executionContext.getStep(), false);
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
            vmHandler.updateDisksFromDb(getVm());
        }
        List<LunDisk> lunDisks = DisksFilter.filterLunDisks(getVm().getDiskMap().values());

        Map<StorageType, List<StorageServerConnections>> connectionsByType =
                lunDisks.stream()
                        .flatMap(d -> storageServerConnectionDao.getAllForLun(d.getLun().getLUNId()).stream())
                        .distinct()
                        .collect(groupingBy(StorageServerConnections::getStorageType, toList()));

        return connectionsByType.entrySet().stream()
                .map(entry -> runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(
                                hostId,
                                getStoragePoolId(),
                                entry.getKey(),
                                entry.getValue())))
                .noneMatch(vdsReturnValue -> !vdsReturnValue.getSucceeded());
    }

    protected boolean updateCinderDisksConnections() {
        if (getVm().getDiskMap().isEmpty()) {
            vmHandler.updateDisksFromDb(getVm());
        }
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(getVm().getDiskMap().values(), ONLY_PLUGGED);
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

    protected void initParametersForExternalNetworks(VDS vds, boolean isMigration) {
        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            VnicProfile vnicProfile = vnicProfileDao.get(iface.getVnicProfileId());
            Network network = networkHelper.getNetworkByVnicProfile(vnicProfile);
            if (network != null && network.isExternal() && iface.isPlugged()) {
                Provider<?> provider = providerDao.get(network.getProvidedBy().getProviderId());
                NetworkProviderProxy providerProxy = providerProxyFactory.create(provider);
                String pluginType = ((OpenstackNetworkProviderProperties) provider.
                    getAdditionalProperties()).getPluginType();
                String hostBindingId = hostProviderBindingDao.get(vds.getId(), pluginType);
                Map<String, String> deviceProperties = providerProxy.allocate(
                    network, vnicProfile, iface, vds, isMigration, hostBindingId);
                getVm().getRuntimeDeviceCustomProperties().put(
                    new VmDeviceId(iface.getId(), getVmId()), deviceProperties);
            }
        }
    }
}
