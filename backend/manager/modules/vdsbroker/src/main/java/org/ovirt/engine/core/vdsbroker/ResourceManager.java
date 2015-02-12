package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
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
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureVDSCommand;
import org.ovirt.vdsm.jsonrpc.client.events.EventSubscriber;
import org.ovirt.vdsm.jsonrpc.client.reactors.ReactorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ResourceManager {

    @Inject
    private BeanManager beanManager;
    //We can't inject event listener due to Jboss 7.1 bug. using programatic lookup instead.
    private volatile IVdsEventListener eventListener;

    private static ResourceManager instance;
    private final Map<Guid, HashSet<Guid>> vdsAndVmsList = new ConcurrentHashMap<>();
    private final Map<Guid, VdsManager> vdsManagersDict = new ConcurrentHashMap<>();
    private final Set<Guid> asyncRunningVms =
            Collections.newSetFromMap(new ConcurrentHashMap<Guid, Boolean>());
    private final ConcurrentHashMap<Guid, VmManager> vmManagers = new ConcurrentHashMap<>();

    private static final String VDSCommandPrefix = "VDSCommand";

    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private int parallelism;

    @Inject
    private AuditLogDirector auditLogDirector;

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
        List<VDS> allVdsList = DbFacade.getInstance().getVdsDao().getAll();
        HashSet<Guid> nonResponsiveVdss = new HashSet<Guid>();
        for (VDS helper_vds : allVdsList) {
            if (helper_vds.getStatus() == VDSStatus.NonResponsive) {
                nonResponsiveVdss.add(helper_vds.getId());
            }
        }

        // Is there any VM that is not fully Up or fully Down?
        boolean runningVmsInTransition = false;

        // Cleanup all vms dynamic data. This is defensive code on power crash
        List<VM> vms = DbFacade.getInstance().getVmDao().getAll();
        for (VM vm : vms) {
            if (!vm.isNotRunning()) {
                if (vm.getRunOnVds() != null) {
                    MultiValueMapUtils.addToMap(vm.getRunOnVds(),
                            vm.getId(),
                            vdsAndVmsList,
                            new MultiValueMapUtils.HashSetCreator<Guid>());
                }
                if (vm.getRunOnVds() != null && nonResponsiveVdss.contains(vm.getRunOnVds())) {
                    SetVmUnknown(vm);
                }
            }

            if (!runningVmsInTransition && vm.isRunning() && vm.getStatus() != VMStatus.Up) {
                runningVmsInTransition = true;
            }
        }

        // Clean pending memory and CPUs if there is no VM in transition on a given Host
        // (meaning we tried to start a VM and the engine crashed before telling VDSM about it)
        List<VdsDynamic> updatedEntities = new ArrayList<>();
        for (VDS _vds : allVdsList) {
            boolean _saveVdsDynamic = false;

            if (_vds.getPendingVcpusCount() != 0 && !runningVmsInTransition) {
                _vds.setPendingVcpusCount(0);
                _saveVdsDynamic = true;
            }

            if (_vds.getPendingVmemSize() != 0 && !runningVmsInTransition) {
                _vds.setPendingVmemSize(0);
                _saveVdsDynamic = true;
            }

            if (_saveVdsDynamic) {
                updatedEntities.add(_vds.getDynamicData());
            }
        }

        DbFacade.getInstance().getVdsDynamicDao().updateAllInBatch(updatedEntities);

        // Populate the VDS dictionary
        for (VDS curVds : allVdsList) {
            AddVds(curVds, true);
        }
        IrsBrokerCommand.init();
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    public boolean AddAsyncRunningVm(Guid vmId) {
        return asyncRunningVms.add(vmId);
    }

    public void RemoveAsyncRunningVm(Guid vmId) {
        asyncRunningVms.remove(vmId);
        getEventListener().removeAsyncRunningCommand(vmId);
    }

    public void succededToRunVm(Guid vmId, Guid vdsId) {
        if (asyncRunningVms.contains(vmId)) {
            getEventListener().runningSucceded(vmId);
        }
        RemoveAsyncRunningVm(vmId);
    }

    /**
     * Initiate rerun event when vm failed to run
     * @param vmId
     */
    public void RerunFailedCommand(Guid vmId, Guid vdsId) {
        if (asyncRunningVms.remove(vmId)) {
            // remove async record from broker only
            getEventListener().rerun(vmId);
        }
    }

    public boolean IsVmInAsyncRunningList(Guid vmId) {
        return asyncRunningVms.contains(vmId);
    }

    public void RemoveVmFromDownVms(Guid vdsId, Guid vmId) {
        HashSet<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            vms.remove(vmId);
        }
    }

    public void handleVmsFinishedInitOnVds(Guid vdsId) {
        HashSet<Guid> vms = vdsAndVmsList.get(vdsId);
        if (vms != null) {
            getEventListener().processOnVmStop(vms);
            vdsAndVmsList.remove(vdsId);
        }
    }

    public IVdsEventListener getEventListener() {
        if (eventListener == null) {
            Bean<?> bean = beanManager.getBeans(IVdsEventListener.class).iterator().next();
            eventListener = (IVdsEventListener) beanManager.getReference(
                    bean,
                    bean.getBeanClass(), beanManager.createCreationalContext(bean));
        }
        return eventListener;
    }

    public void reestablishConnection(Guid vdsId) {
        VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
        RemoveVds(vds.getId());
        AddVds(vds, false);
    }

    public void AddVds(VDS vds, boolean isInternal) {
        VdsManager vdsManager = new VdsManager(vds, auditLogDirector);
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

    public void RemoveVds(Guid vdsId) {
        RemoveVds(vdsId, false);
    }

    public void RemoveVds(Guid vdsId, boolean newHost) {
        VdsManager vdsManager = GetVdsManager(vdsId, newHost);
        if (vdsManager != null) {
            vdsManager.dispose();
            vdsManagersDict.remove(vdsId);
        }
    }

    public VdsManager GetVdsManager(Guid vdsId) {
        return GetVdsManager(vdsId, false);
    }

    public VdsManager GetVdsManager(Guid vdsId, boolean newHost) {
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
     * @param vm
     */
    public void SetVmUnknown(VM vm) {
        RemoveAsyncRunningVm(vm.getId());
        InternalSetVmStatus(vm, VMStatus.Unknown);
        // log VM transition to unknown status
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVmId(vm.getId());
        auditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);

        storeVm(vm);

    }

    private void storeVm(VM vm) {
        DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
        DbFacade.getInstance().getVmStatisticsDao().update(vm.getStatisticsData());
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces != null) {
            for (VmNetworkInterface ifc : interfaces) {
                VmNetworkStatistics stats = ifc.getStatistics();
                DbFacade.getInstance().getVmNetworkStatisticsDao().update(stats);
            }
        }
    }

    public boolean IsVmDuringInitiating(Guid vm_guid) {
        return asyncRunningVms.contains(vm_guid);
    }

    /**
     * Set vm status without saving to DB
     *
     * <p> Note: Calling this method with status=down, must be only when the
     *     VM went down normally, otherwise call {@link #InternalSetVmStatus(VM, VMStatus, VmExitStatus, String)}
     *
     * @param vm
     * @param status
     */
    public void InternalSetVmStatus(VM vm, final VMStatus status) {
        InternalSetVmStatus(vm, status, VmExitStatus.Normal, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void InternalSetVmStatus(VM vm, final VMStatus status, VmExitStatus exitStatus) {
        InternalSetVmStatus(vm, status, exitStatus, StringUtils.EMPTY, VmExitReason.Unknown);
    }

    public void InternalSetVmStatus(VM vm, final VMStatus status, final VmExitStatus exitStaus, final String exitMessage, final VmExitReason exitReason) {
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

    public void UpdateVdsStatisticsData(VdsStatistics vdsStatistics) {
        VdsManager vdsManager = GetVdsManager(vdsStatistics.getId());
        if (vdsManager != null) {
            vdsManager.updateStatisticsData(vdsStatistics);
        }
    }

    private static String GetCommandTypeName(VDSCommandType command) {
        String packageName = command.getPackageName();
        String commandName = String.format("%s.%s%s", packageName, command, VDSCommandPrefix);
        return commandName;
    }

    /**
     * Create the command which needs to run.
     * @param <P>
     * @param commandType
     * @param parameters
     * @return The command, or null if it can't be created.
     */
    private <P extends VDSParametersBase> VDSCommandBase<P> CreateCommand(
            VDSCommandType commandType, P parameters) {
        try {
            @SuppressWarnings("unchecked")
            Class<VDSCommandBase<P>> type =
                    (Class<VDSCommandBase<P>>) Class.forName(GetCommandTypeName(commandType));
            Constructor<VDSCommandBase<P>> constructor =
                    ReflectionUtils.findConstructor(type, parameters.getClass());

            if (constructor != null) {
                VDSCommandBase<P> cmd = constructor.newInstance(new Object[] { parameters });
                InjectionTarget injectionTarget =
                        beanManager.createInjectionTarget(beanManager.createAnnotatedType(cmd.getClass()));
                injectionTarget.inject(cmd, beanManager.createCreationalContext(null));
                return cmd;
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                log.error("CreateCommand failed: {}", e.getCause().getMessage());
                log.error("Exception", e);
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.error("CreateCommand failed: {}", e.getMessage());
            log.debug("Exception", e);
        }
        return null;
    }

    private <P extends VdsIdVDSCommandParametersBase> FutureVDSCommand createFutureCommand(FutureVDSCommandType commandType,
            P parameters) {
        try {
            Class<FutureVDSCommand> type =
                    (Class<FutureVDSCommand>) Class.forName(commandType.getFullyQualifiedClassName());
            Constructor<FutureVDSCommand> constructor =
                    ReflectionUtils.findConstructor(type,
                            parameters.getClass());

            if (constructor != null) {
                return constructor.newInstance(new Object[] { parameters });
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
        VDSCommandBase<P> command = CreateCommand(commandType, parameters);

        if (command != null) {
            command.execute();
            return command.getVDSReturnValue();
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
            vmManagers.putIfAbsent(vmId, new VmManager(vmId));
        }
        return vmManagers.get(vmId);
    }

    public void onVmDelete(@Observes @VmDeleted Guid vmId) {
        vmManagers.remove(vmId);
    }

    public void subscribe(EventSubscriber subscriber) {
        ReactorFactory.getWorker(this.parallelism).getPublisher().subscribe(subscriber);
    }
}
