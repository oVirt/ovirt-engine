package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureVDSCommand;

public class ResourceManager {

    private static ResourceManager _Instance = new ResourceManager();
    private final Map<Guid, HashSet<Guid>> _vdsAndVmsList = new ConcurrentHashMap<Guid, HashSet<Guid>>();
    private final Map<Guid, VdsManager> _vdsManagersDict = new ConcurrentHashMap<Guid, VdsManager>();
    private final ConcurrentHashMap<Guid, Boolean> _asyncRunningVms =
            new ConcurrentHashMap<Guid, Boolean>();

    private static final String VDSCommandPrefix = "VDSCommand";

    private static Log log = LogFactory.getLog(ResourceManager.class);

    private ResourceManager() {

    }

    public static ResourceManager getInstance() {
        return _Instance;
    }

    public void init() {
        log.info("ResourceManager::ResourceManager::Entered");
        List<VDS> allVdsList = DbFacade.getInstance().getVdsDao().getAll();
        HashSet<Guid> nonResponsiveVdss = new HashSet<Guid>();
        for (VDS helper_vds : allVdsList) {
            if (helper_vds.getstatus() == VDSStatus.NonResponsive) {
                nonResponsiveVdss.add(helper_vds.getId());
            }
        }

        // Cleanup all vms dynamic data. This is defencive code on power crash
        List<VM> vms = DbFacade.getInstance().getVmDao().getAll();
        for (VM vm : vms) {
            if (!VM.isStatusDown(vm.getStatus())) {
                // check if vm should be suspended
                if (vm.getStatus() == VMStatus.SavingState) {
                    InternalSetVmStatus(vm, VMStatus.Suspended);
                    DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
                    DbFacade.getInstance().getVmStatisticsDao().update(vm.getStatisticsData());
                } else {
                    if (vm.getRunOnVds() != null) {
                        MultiValueMapUtils.addToMap(vm.getRunOnVds().getValue(),
                                vm.getId(),
                                _vdsAndVmsList,
                                new MultiValueMapUtils.HashSetCreator<Guid>());
                    }
                    if (vm.getRunOnVds() != null && nonResponsiveVdss.contains(vm.getRunOnVds())) {
                        SetVmUnknown(vm);
                    }
                }
            }
        }
        // Populate the VDS dictionary
        for (VDS curVds : allVdsList) {
            AddVds(curVds, true);
        }
        IrsBrokerCommand.Init();
    }

    public boolean AddAsyncRunningVm(Guid vmId) {
        boolean returnValue = false;
        if (_asyncRunningVms.putIfAbsent(vmId, Boolean.TRUE) == null) {
            returnValue = true;
        }
        return returnValue;
    }

    public void RemoveAsyncRunningVm(Guid vmId) {
        _asyncRunningVms.remove(vmId);
        getEventListener().removeAsyncRunningCommand(vmId);
    }

    public void SuccededToRunVm(Guid vmId, Guid vdsId) {
        if (_asyncRunningVms.containsKey(vmId)) {
            getEventListener().runningSucceded(vmId);
        }
        RemoveAsyncRunningVm(vmId);
    }

    /**
     * Initiate rerun event when vm failed to run
     * @param vmId
     */
    public void RerunFailedCommand(Guid vmId, Guid vdsId) {
        Boolean value = _asyncRunningVms.remove(vmId);
        // remove async record from broker only
        if (value != null) {
            getEventListener().rerun(vmId);
        }
    }

    public boolean IsVmInAsyncRunningList(Guid vmId) {
        return (_asyncRunningVms.containsKey(vmId));
    }

    public void RemoveVmFromDownVms(Guid vdsId, Guid vmId) {
        HashSet<Guid> vms = null;
        if ((vms = _vdsAndVmsList.get(vdsId)) != null) {
            vms.remove(vmId);
        }
    }

    public void HandleVdsFinishedInit(Guid vdsId) {
        HashSet<Guid> vms = null;
        if ((vms = _vdsAndVmsList.get(vdsId)) != null) {
            for (Guid vmId : vms) {
                getEventListener().processOnVmStop(vmId);
                log.info("Procceed on vm stop entered: " + vmId.toString());
            }
            _vdsAndVmsList.remove(vdsId);
        }
    }

    public IVdsEventListener getEventListener() {
        return EjbUtils.findBean(BeanType.VDS_EVENT_LISTENER, BeanProxyType.LOCAL);
    }

    public void AddVds(VDS vds, boolean isInternal) {
        VdsManager vdsManager = VdsManager.buildVdsManager(vds);
        if (isInternal) {
            VDSStatus status = vds.getstatus();
            switch (vds.getstatus()) {
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
            if (status != vds.getstatus()) {
                vdsManager.setStatus(status, vds);
                vdsManager.UpdateStatisticsData(vds.getStatisticsData());
            }

            // set pending to 0
            vds.setpending_vcpus_count(0);
            vdsManager.UpdateDynamicData(vds.getDynamicData());
        }
        vdsManager.schedulJobs();
        _vdsManagersDict.put(vds.getId(), vdsManager);
        log.infoFormat("ResourceManager::AddVds - VDS {0} was added to the Resource Manager", vds.getId());

    }

    public void RemoveVds(Guid vdsId) {
        VdsManager vdsManager = GetVdsManager(vdsId);
        if (vdsManager != null) {
            vdsManager.dispose();
            _vdsManagersDict.remove(vdsId);
        }
    }

    public VdsManager GetVdsManager(Guid vdsId) {
        VdsManager vdsManger = _vdsManagersDict.get(vdsId);
        if (vdsManger == null) {
            log.errorFormat("Cannot get vdsManager for vdsid={0}", vdsId);
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
        AuditLogDirector.log(logable, AuditLogType.VM_SET_TO_UNKNOWN_STATUS);

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
        return _asyncRunningVms.containsKey(vm_guid);
    }

    /**
     * Set vm status without saving to DB
     * @param vm
     * @param status
     */
    public void InternalSetVmStatus(VM vm, VMStatus status) {
        vm.setStatus(status);
        VMStatus vmStatus = vm.getStatus();
        boolean isVmStatusDown = VM.isStatusDown(vmStatus);

        if (isVmStatusDown || vmStatus == VMStatus.Unknown) {
            resetVmAttributes(vm);

            if (isVmStatusDown) {
                vm.setRunOnVds(null);
                vm.setVmPauseStatus(VmPauseStatus.NONE);
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
        vm.setMigratingToVds(null);
        vm.setRunOnVdsName("");
        vm.setGuestCurrentUserName(null);
        vm.setConsoleCurrentUserName(null);
        vm.setConsoleUserId(null);
        vm.setGuestOs(null);
        vm.setVmIp(null);
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        for (VmNetworkInterface ifc : interfaces) {
            NetworkStatistics statistics = ifc.getStatistics();
            statistics.setTransmitDropRate(0D);
            statistics.setTransmitRate(0D);
            statistics.setReceiveRate(0D);
            statistics.setReceiveDropRate(0D);
        }
    }

    public void UpdateVdsDynamicData(VdsDynamic vdsDynamic) {
        VdsManager vdsManager = GetVdsManager(vdsDynamic.getId());
        if (vdsManager != null) {
            vdsManager.UpdateDynamicData(vdsDynamic);
        }
    }

    public void UpdateVdsStatisticsData(VdsStatistics vdsStatistics) {
        VdsManager vdsManager = GetVdsManager(vdsStatistics.getId());
        if (vdsManager != null) {
            vdsManager.UpdateStatisticsData(vdsStatistics);
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
                return constructor.newInstance(new Object[] { parameters });
            }
        } catch (Exception e) {
            if (e.getCause() != null) {
                log.debug("CreateCommand failed", e.getCause());
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.debug("CreateCommand failed", e);
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
                log.debug("CreateCommand failed", e.getCause());
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.debug("CreateCommand failed", e);
        }
        return null;

    }

    public <P extends VDSParametersBase> VDSReturnValue runVdsCommand(VDSCommandType commandType, P parameters) {
        // try run vds command
        VDSCommandBase<P> command = CreateCommand(commandType, parameters);

        if (command != null) {
            command.Execute();
            return command.getVDSReturnValue();
        }

        return null;
    }

    public <P extends VdsIdVDSCommandParametersBase> FutureVDSCall<VDSReturnValue> runFutureVdsCommand(final FutureVDSCommandType commandType,
            final P parameters) {
        FutureVDSCommand<P> command = createFutureCommand(commandType, parameters);

        if (command != null) {
            command.Execute();
            return command;
        }

        return null;
    }
}
