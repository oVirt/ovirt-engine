package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.di.interceptor.InvocationLogger;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSAsyncReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.vdsbroker.monitoring.HostMonitoringWatchdog;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsCommandExecutor;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InvocationLogger
@Singleton
public class ResourceManager implements BackendService {

    private final Map<Guid, Set<Guid>> vdsAndVmsList = new ConcurrentHashMap<>();
    private final Map<Guid, VdsManager> vdsManagersDict = new ConcurrentHashMap<>();
    private final Set<Guid> asyncRunningVms =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<Guid, VmManager> vmManagers = new ConcurrentHashMap<>();

    private static final String VDSCommandPrefix = "VDSCommand";

    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private int parallelism = Config.getValue(ConfigValues.EventProcessingPoolSize);
    private int eventTimeoutInHours = Config.getValue(ConfigValues.EventPurgeTimeoutInHours);

    private HostMonitoringWatchdog hostMonitoringWatchdog;

    @Inject
    private Instance<IVdsEventListener> eventListener;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsDao hostDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;

    @Inject
    Instance<VdsCommandExecutor> commandExecutor;

    @Inject
    private VdsManagerFactory vdsManagerFactory;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        populateVdsAndVmsList();

        // Populate the VDS dictionary
        final List<VDS> allVdsList = hostDao.getAll();
        for (VDS curVds : allVdsList) {
            addVds(curVds, true, false);
        }
        hostMonitoringWatchdog = new HostMonitoringWatchdog(monitoringExecutor, hostDao, () -> vdsManagersDict);
        hostMonitoringWatchdog.start();
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void populateVdsAndVmsList() {
        final List<VmDynamic> vms = vmDynamicDao.getAll();
        vdsAndVmsList.putAll(vms.stream()
                .filter(vm -> !vm.getStatus().isNotRunning() && vm.getRunOnVds() != null)
                .collect(Collectors.groupingBy(VmDynamic::getRunOnVds,
                        Collectors.mapping(VmDynamic::getId, Collectors.toSet()))));
    }

    public boolean addAsyncRunningVm(Guid vmId) {
        return asyncRunningVms.add(vmId);
    }

    public void removeAsyncRunningVm(Guid vmId) {
        asyncRunningVms.remove(vmId);
        getEventListener().removeAsyncRunningCommand(vmId);
    }

    public void succededToRunVm(Guid vmId, Guid vdsId) {
        if (asyncRunningVms.contains(vmId)) {
            getEventListener().runningSucceded(vmId);
        }
        removeAsyncRunningVm(vmId);
    }

    /**
     * Initiate rerun event when vm failed to run
     */
    public void rerunFailedCommand(Guid vmId, Guid vdsId) {
        if (asyncRunningVms.remove(vmId)) {
            // remove async record from broker only
            getEventListener().rerun(vmId);
        }
    }

    public boolean isVmInAsyncRunningList(Guid vmId) {
        return asyncRunningVms.contains(vmId);
    }

    public void removeVmFromDownVms(Guid vdsId, Guid vmId) {
        Set<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            vms.remove(vmId);
        }
    }

    public void handleVmsFinishedInitOnVds(Guid vdsId) {
        Set<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            getEventListener().processOnVmStop(vms, vdsId);
            vdsAndVmsList.remove(vdsId);
        }
    }

    public IVdsEventListener getEventListener() {
        return eventListener.get();
    }

    public void addVds(VDS vds, boolean isInternal, boolean scheduleJobs) {
        VdsManager vdsManager = vdsManagerFactory.create(vds, this);
        if (isInternal) {
            VDSStatus status = vds.getStatus();
            switch (vds.getStatus()) {
            case Error:
                status = VDSStatus.Up;
                break;
            case Reboot:
            case NonResponsive:
            case Connecting:
            case Installing:
                status = VDSStatus.Unassigned;
                break;
            }
            if (status != vds.getStatus()) {
                vdsManager.setStatus(status, vds);
                vdsManager.updateStatisticsData(vds.getStatisticsData());
            }

            // set pending to 0
            vds.setPendingVcpusCount(0);
            vdsManager.updateDynamicData(vds.getDynamicData());
        }

        if (scheduleJobs) {
            vdsManager.scheduleJobs();
        }

        vdsManagersDict.put(vds.getId(), vdsManager);
        log.info("VDS '{}' was added to the Resource Manager", vds.getId());

    }

    public void scheduleJobsForHosts() {
        vdsManagersDict.values().forEach(VdsManager::scheduleJobs);
    }

    public void removeVds(Guid vdsId) {
        removeVds(vdsId, false);
    }

    public void removeVds(Guid vdsId, boolean newHost) {
        VdsManager vdsManager = getVdsManager(vdsId, newHost);
        if (vdsManager != null) {
            vdsManager.dispose();
            vdsManagersDict.remove(vdsId);
        }
    }

    public VdsManager getVdsManager(Guid vdsId) {
        return getVdsManager(vdsId, false);
    }

    public VdsManager getVdsManager(Guid vdsId, boolean newHost) {
        VdsManager vdsManger = vdsManagersDict.get(vdsId);
        if (vdsManger == null) {
            if (!newHost) {
                log.error("Cannot get vdsManager for vdsid='{}'.", vdsId);
            }
        }
        return vdsManger;
    }

    /**
     * Set vm status to Unknown and save to DB.
     */
    public void setVmUnknown(VM vm) {
        removeAsyncRunningVm(vm.getId());
        internalSetVmStatus(vm.getDynamicData(), VMStatus.Unknown);
        // log VM transition to unknown status
        AuditLogable logable = new AuditLogableImpl();
        logable.setVmId(vm.getId());
        logable.setVmName(vm.getName());
        auditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);

        storeVm(vm);

    }

    private void storeVm(VM vm) {
        vmDynamicDao.update(vm.getDynamicData());
        getVmManager(vm.getId()).update(vm.getStatisticsData());
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces != null) {
            for (VmNetworkInterface ifc : interfaces) {
                VmNetworkStatistics stats = ifc.getStatistics();
                vmNetworkStatisticsDao.update(stats);
            }
        }
    }

    public boolean isVmDuringInitiating(Guid vm_guid) {
        return asyncRunningVms.contains(vm_guid);
    }

    /**
     * Set vm status without saving to DB
     *
     * <p>
     * Note: Calling this method with status=down, must be only when the VM went down normally, otherwise call
     * {@link #InternalSetVmStatus(VM, VMStatus, VmExitStatus, String)}
     */
    public void internalSetVmStatus(VmDynamic vm, final VMStatus status) {
        internalSetVmStatus(vm, status, VmExitStatus.Normal, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void internalSetVmStatus(VmDynamic vm, final VMStatus status, VmExitStatus exitStatus) {
        internalSetVmStatus(vm, status, exitStatus, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void internalSetVmStatus(VmDynamic vm,
            final VMStatus status,
            final VmExitStatus exitStaus,
            final String exitMessage,
            final VmExitReason exitReason) {
        vm.setStatus(status);
        vm.setExitStatus(exitStaus);
        vm.setExitMessage(exitMessage);
        vm.setExitReason(exitReason);

        boolean isVmNotRunning = status.isNotRunning();

        if (isVmNotRunning || status == VMStatus.Unknown) {
            resetVmAttributes(vm);

            if (isVmNotRunning) {
                vm.setRunOnVds(null);
                vm.setPauseStatus(VmPauseStatus.NONE);
                vm.setLastStopTime(new Date());
                if (status == VMStatus.Down) {
                    vm.setBootTime(null);
                    vm.setDowntime(0);
                    vm.setRuntimeName(null);
                    vm.setCurrentCpuPinning(null);
                    vm.setCurrentSockets(0);
                    vm.setCurrentCoresPerSocket(0);
                    vm.setCurrentThreadsPerCore(0);
                }
            }
        }
    }

    /**
     * Resets VM attributes
     * @param vm
     *            the VM to reset
     */
    private void resetVmAttributes(VmDynamic vm) {
        vm.setMigratingToVds(null);
        vm.getGraphicsInfos().clear();
        vm.setGuestCurrentUserName(null);
        vm.setConsoleCurrentUserName(null);
        vm.setConsoleUserId(null);
        vm.setClientIp(null);
        vm.setIp(null);
        vm.setFqdn(null);
        vm.setCpuName(null);
        vm.setEmulatedMachine(null);
        vm.setVolatileRun(false);
        vm.setGuestAgentNicsHash(0);
    }

    private static String getCommandTypeName(VDSCommandType command) {
        String packageName = command.getPackageName();
        String commandName = String.format("%s.%s%s", packageName, command, VDSCommandPrefix);
        return commandName;
    }

    /**
     * Create the command which needs to run.
     * @return The command, or null if it can't be created.
     */
    private <P extends VDSParametersBase> VDSCommandBase<P> createCommand(
            VDSCommandType commandType,
            P parameters) {
        try {
            @SuppressWarnings("unchecked")
            Class<VDSCommandBase<P>> type =
                    (Class<VDSCommandBase<P>>) Class.forName(getCommandTypeName(commandType));
            Constructor<VDSCommandBase<P>> constructor =
                    ReflectionUtils.findConstructor(type, parameters.getClass());

            if (constructor != null) {
                return instantiateInjectedCommand(parameters, constructor);
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                log.error("createCommand failed: {}", e.getCause().getMessage());
                log.error("Exception", e);
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.error("createCommand failed: {}", e.getMessage());
            log.debug("Exception", e);
        }
        return null;
    }

    private <P extends VDSParametersBase, T extends VDSCommandBase<P>> T instantiateInjectedCommand(P parameters,
            Constructor<T> constructor) throws Exception {
        T cmd = constructor.newInstance(new Object[] { parameters });
        Injector.injectMembers(cmd);
        return cmd;
    }

    private <P extends VdsIdVDSCommandParametersBase> FutureVDSCommand<P> createFutureCommand(
            FutureVDSCommandType commandType,
            P parameters) {
        try {
            Class<FutureVDSCommand<P>> type =
                    (Class<FutureVDSCommand<P>>) Class.forName(commandType.getFullyQualifiedClassName());
            Constructor<FutureVDSCommand<P>> constructor = ReflectionUtils.findConstructor(type, parameters.getClass());

            if (constructor != null) {
                return instantiateInjectedCommand(parameters, constructor);
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                log.error("CreateFutureCommand failed: {}", e.getCause().getMessage());
                log.debug("Exception", e);
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.error("CreateFutureCommand failed: {}", e.getMessage());
            log.debug("Exception", e);
        }
        return null;

    }

    public <P extends VDSParametersBase> VDSReturnValue runVdsCommand(VDSCommandType commandType, P parameters) {
        // try run vds command
        VDSCommandBase<P> command = createCommand(commandType, parameters);

        if (command != null) {
            return commandExecutor.get().execute(command, commandType);
        }

        return null;
    }

    public <P extends VDSParametersBase> VDSAsyncReturnValue runAsyncVdsCommand(VDSCommandType commandType,
            P parameters) {
        VDSCommandBase<P> command = createCommand(commandType, parameters);

        if (command != null) {
            command.setAsync(true);
            commandExecutor.get().execute(command, commandType);

            VDSReturnValue value = command.getVDSReturnValue();
            if (!VDSAsyncReturnValue.class.isInstance(value)) {
                throw new IllegalStateException("Wrong return value type");
            }
            return (VDSAsyncReturnValue) value;
        }

        return null;
    }

    public <P extends VdsIdVDSCommandParametersBase> FutureVDSCall<VDSReturnValue> runFutureVdsCommand(
            final FutureVDSCommandType commandType,
            final P parameters) {
        FutureVDSCommand<P> command = createFutureCommand(commandType, parameters);

        if (command != null) {
            command.execute();
            return command;
        }

        return null;
    }

    public VmManager getVmManager(Guid vmId) {
        return getVmManager(vmId, true);
    }

    public VmManager getVmManager(Guid vmId, boolean createIfAbsent) {
        if (createIfAbsent && !vmManagers.containsKey(vmId)) {
            vmManagers.computeIfAbsent(vmId, guid -> Injector.injectMembers(new VmManager(guid)));
        }
        return vmManagers.get(vmId);
    }

    public void clearLastStatusEventStampsFromVds(Guid vdsId) {
        for (VmManager vmManager : vmManagers.values()) {
            vmManager.clearLastStatusEventStampIfFromVds(vdsId);
        }
    }

    public void onVmDelete(@Observes @VmDeleted Guid vmId) {
        vmManagers.remove(vmId);
    }

    public void subscribe(EventSubscriber subscriber) {
        log.debug("subscribe called with subscription id: {}", subscriber.getSubscriptionId());
        ReactorFactory.getWorker(this.parallelism, this.eventTimeoutInHours).getPublisher().subscribe(subscriber);
    }

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineThreadMonitoringThreadPool)
    private ManagedScheduledExecutorService monitoringExecutor;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public Map<String, Pair<String, String>> getVdsPoolAndStorageConnectionsLock(Guid vdsId) {
        return getEventListener().getVdsPoolAndStorageConnectionsLock(vdsId);
    }

}
