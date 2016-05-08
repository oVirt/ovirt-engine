package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
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
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSAsyncReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.monitoring.MonitoringStrategyFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureVDSCommand;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ResourceManager implements BackendService {

    private static ResourceManager instance;
    private final Map<Guid, HashSet<Guid>> vdsAndVmsList = new ConcurrentHashMap<>();
    private final Map<Guid, VdsManager> vdsManagersDict = new ConcurrentHashMap<>();
    private final Set<Guid> asyncRunningVms =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<Guid, VmManager> vmManagers = new ConcurrentHashMap<>();

    private static final String VDSCommandPrefix = "VDSCommand";

    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private int parallelism;

    @Inject
    private Instance<IVdsEventListener> eventListener;

    @Inject
    private BeanManager beanManager;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsDao hostDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;

    @Inject
    private DbFacade dbFacade;

    @Inject
    private MonitoringStrategyFactory monitoringStrategyFactory;

    private ResourceManager() {
        this.parallelism = Config.<Integer> getValue(ConfigValues.EventProcessingPoolSize);
    }

    /**
     * TODO remove this after moving all places to use CDI.
     * kept for backward compatibility.
     */
    @Deprecated
    public static ResourceManager getInstance() {
        return instance;
    }

    private static void setInstance(ResourceManager manager) {
        instance = manager;
    }

    @PostConstruct
    private void init() {
        // init the singleton. TODO remove once all code is using CDI
        setInstance(this);

        log.info("Start initializing {}", getClass().getSimpleName());
        populateVdsAndVmsList();

        // Populate the VDS dictionary
        final List<VDS> allVdsList = hostDao.getAll();
        for (VDS curVds : allVdsList) {
            addVds(curVds, true);
        }
        IrsBrokerCommand.init();

        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void populateVdsAndVmsList() {

        final List<VM> vms = vmDao.getAll();

        for (VM vm : vms) {
            if (!vm.isNotRunning() && vm.getRunOnVds() != null) {
                MultiValueMapUtils.addToMap(vm.getRunOnVds(),
                        vm.getId(),
                        vdsAndVmsList,
                        new MultiValueMapUtils.HashSetCreator<>());
            }
        }
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
        HashSet<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            vms.remove(vmId);
        }
    }

    public void handleVmsFinishedInitOnVds(Guid vdsId) {
        HashSet<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            getEventListener().processOnVmStop(vms, vdsId);
            vdsAndVmsList.remove(vdsId);
        }
    }

    public IVdsEventListener getEventListener() {
        return eventListener.get();
    }

    public void reestablishConnection(Guid vdsId) {
        VDS vds = hostDao.get(vdsId);
        removeVds(vds.getId());
        addVds(vds, false);
    }

    public void addVds(VDS vds, boolean isInternal) {
        VdsManager vdsManager = new VdsManager(
                vds,
                auditLogDirector,
                this,
                dbFacade,
                monitoringStrategyFactory);
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
        vdsManager.scheduleJobs();
        vdsManagersDict.put(vds.getId(), vdsManager);
        log.info("VDS '{}' was added to the Resource Manager", vds.getId());

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
        internalSetVmStatus(vm, VMStatus.Unknown);
        // log VM transition to unknown status
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVmId(vm.getId());
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
     * <p> Note: Calling this method with status=down, must be only when the
     *     VM went down normally, otherwise call {@link #InternalSetVmStatus(VM, VMStatus, VmExitStatus, String)}
     */
    public void internalSetVmStatus(VM vm, final VMStatus status) {
        internalSetVmStatus(vm, status, VmExitStatus.Normal, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void internalSetVmStatus(VM vm, final VMStatus status, VmExitStatus exitStatus) {
        internalSetVmStatus(vm, status, exitStatus, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void internalSetVmStatus(VM vm,
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
                vm.setVmPauseStatus(VmPauseStatus.NONE);
                vm.setLastStopTime(new Date());
            }
        }
    }

    /**
     * Resets VM attributes
     * @param vm
     *            the VM to reset
     */
    private void resetVmAttributes(VM vm) {
        vm.setUsageNetworkPercent(0);
        vm.setElapsedTime(0D);
        vm.setCpuSys(0D);
        vm.setCpuUser(0D);
        vm.setUsageCpuPercent(0);
        vm.setUsageMemPercent(0);
        vm.setMemoryUsageHistory(null);
        vm.setCpuUsageHistory(null);
        vm.setNetworkUsageHistory(null);
        vm.setMigratingToVds(null);
        vm.setRunOnVdsName("");
        vm.getGraphicsInfos().clear();
        vm.setGuestCurrentUserName(null);
        vm.setConsoleCurrentUserName(null);
        vm.setConsoleUserId(null);
        vm.setGuestOs(null);
        vm.setVmIp(null);
        vm.setVmFQDN(null);
        vm.setCpuName(null);
        vm.setEmulatedMachine(null);
        vm.setMigrationProgressPercent(0);
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        for (VmNetworkInterface ifc : interfaces) {
            NetworkStatistics statistics = ifc.getStatistics();
            statistics.setTransmitDropRate(0D);
            statistics.setTransmitRate(0D);
            statistics.setReceiveRate(0D);
            statistics.setReceiveDropRate(0D);
        }
        List<VmNumaNode> vmNumaNodes = vm.getvNumaNodeList();
        for (VmNumaNode node : vmNumaNodes) {
            node.getVdsNumaNodeList().clear();
        }
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
            VDSCommandType commandType, P parameters) {
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
        InjectionTarget injectionTarget =
                beanManager.createInjectionTarget(beanManager.createAnnotatedType(cmd.getClass()));
        injectionTarget.inject(cmd, beanManager.createCreationalContext(null));
        return cmd;
    }

    private <P extends VdsIdVDSCommandParametersBase> FutureVDSCommand<P> createFutureCommand(FutureVDSCommandType commandType,
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
            command.execute();
            return command.getVDSReturnValue();
        }

        return null;
    }

    public <P extends VDSParametersBase> VDSAsyncReturnValue runAsyncVdsCommand(VDSCommandType commandType, P parameters) {
        VDSCommandBase<P> command = createCommand(commandType, parameters);

        if (command != null) {
            command.setAsync(true);
            command.execute();

            VDSReturnValue value = command.getVDSReturnValue();
            if (!VDSAsyncReturnValue.class.isInstance(value)) {
                throw new IllegalStateException("Wrong return value type");
            }
            return (VDSAsyncReturnValue) value;
        }

        return null;
    }

    public <P extends VdsIdVDSCommandParametersBase> FutureVDSCall<VDSReturnValue> runFutureVdsCommand(final FutureVDSCommandType commandType,
            final P parameters) {
        FutureVDSCommand<P> command = createFutureCommand(commandType, parameters);

        if (command != null) {
            command.execute();
            return command;
        }

        return null;
    }

    public VmManager getVmManager(Guid vmId) {
        if (!vmManagers.containsKey(vmId)) {
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
        ReactorFactory.getWorker(this.parallelism).getPublisher().subscribe(subscriber);
    }

    public boolean isUpdateAvailable(VDS host) {
        return getEventListener().isUpdateAvailable(host);
    }
}
