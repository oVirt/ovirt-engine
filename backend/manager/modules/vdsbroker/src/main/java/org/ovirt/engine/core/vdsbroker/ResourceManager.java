package org.ovirt.engine.core.vdsbroker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureVDSCommand;

public class ResourceManager implements IVdsEventListener {
    private static Log log = LogFactory.getLog(ResourceManager.class);
    private static ResourceManager _Instance = new ResourceManager();

    public static ResourceManager getInstance() {
        return _Instance;
    }

    private ResourceManager() {
        log.info("ResourceManager::ResourceManager::Entered");
        List<VDS> allVdsList = DbFacade.getInstance().getVdsDAO().getAll();
        HashSet<Guid> nonResponsiveVdss = new HashSet<Guid>();
        for (VDS helper_vds : allVdsList)
        {
            if (helper_vds.getstatus() == VDSStatus.NonResponsive)
            {
                nonResponsiveVdss.add(helper_vds.getId());
            }
        }

        // Cleanup all vms dynamic data. This is defencive code on power crash
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAll();
        for (VM vm : vms) {
            if (!VM.isStatusDown(vm.getstatus())) {
                // check if vm should be suspended
                if (vm.getstatus() == VMStatus.SavingState) {
                    InternalSetVmStatus(vm, VMStatus.Suspended);
                    DbFacade.getInstance().getVmDynamicDAO().update(vm.getDynamicData());
                    DbFacade.getInstance().getVmStatisticsDAO().update(vm.getStatisticsData());
                } else {
                    if (vm.getrun_on_vds() != null) {
                        HashSet<Guid> vmsList = null;
                        if (!((vmsList = _vdsAndVmsList.get(vm.getrun_on_vds())) != null)) {
                            vmsList = new HashSet<Guid>();
                        }
                        vmsList.add(vm.getId());
                        _vdsAndVmsList.put(new Guid(vm.getrun_on_vds().toString()), vmsList);
                    }
                    if (vm.getrun_on_vds() != null && nonResponsiveVdss.contains(vm.getrun_on_vds())) {
                        SetVmUnknown(vm);
                    }
                }
            }
        }
        if (allVdsList.size() > 0) {
            int sleepTimout = Config.<Integer> GetValue(ConfigValues.VdsRefreshRate)
                    * Config.<Integer> GetValue(ConfigValues.NumberVmRefreshesBeforeSave) * 1000 / allVdsList.size();
            // Populate the VDS dictionary
            for (VDS curVds : allVdsList) {
                log.debugFormat("Putting thread to sleep for {0} milliseconds before adding VDS {1}, with name {2}",
                        sleepTimout, curVds.getId(), curVds.getvds_name());
                ThreadUtils.sleep(sleepTimout);
                AddVds(curVds, true);
            }
        }
        IrsBrokerCommand.Init();
    }

    private final HashMap<Guid, HashSet<Guid>> _vdsAndVmsList =
            new HashMap<Guid, HashSet<Guid>>();
    private final Map<Guid, VdsManager> _vdsManagersDict = new ConcurrentHashMap<Guid, VdsManager>();
    private final ConcurrentHashMap<Guid, IVdsEventListener> _asyncRunningVms =
            new ConcurrentHashMap<Guid, IVdsEventListener>();

    public boolean AddAsyncRunningVm(Guid vmId, IVdsEventListener listener) {
        boolean returnValue = false;
        if (_asyncRunningVms.putIfAbsent(vmId, listener) == null) {
            returnValue = true;
        }
        return returnValue;
    }

    public void RemoveAsyncRunningVm(Guid vmId) {
        _asyncRunningVms.remove(vmId);
        getEventListener().removeAsyncRunningCommand(vmId);
    }

    public void SuccededToRunVm(Guid vmId, Guid vdsId) {
        IVdsEventListener listener = _asyncRunningVms.get(vmId);
        if (listener != null) {
            listener.runningSucceded(vmId);
        }
        RemoveAsyncRunningVm(vmId);
    }

    /**
     * Initiate rerun event when vm failed to run
     *
     * @param vmId
     */
    public void RerunFailedCommand(Guid vmId, Guid vdsId) {
        IVdsEventListener listener = _asyncRunningVms.remove(vmId);
        // remove async record from broker only
        if (listener != null) {
            listener.rerun(vmId);
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

    // if no event listener, return this (no implementation)
    public IVdsEventListener getEventListener() {
        IVdsEventListener eventListener = EjbUtils.findBean(BeanType.VDS_EVENT_LISTENER, BeanProxyType.LOCAL);
        if (eventListener != null) {
            return eventListener;
        } else {
            return this;
        }
    }

    /**
     * This will return the EventListener from WCF Callback Channel if exists or the EventListener if not
     */
    public IVdsEventListener getBackendCallback() {
        return getEventListener();
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
            case Problematic:
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
        if (_vdsManagersDict.containsKey(vdsId)) {
            return _vdsManagersDict.get(vdsId);
        } else {
            log.errorFormat("Cannot get vdsManager for vdsid={0}", vdsId);
        }
        return null;
    }

    /**
     * Set vm status to DOWN and save to DB.
     *
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
        DbFacade.getInstance().getVmDynamicDAO().update(vm.getDynamicData());
        DbFacade.getInstance().getVmStatisticsDAO().update(vm.getStatisticsData());
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces != null) {
            for (VmNetworkInterface ifc : interfaces) {
                VmNetworkStatistics stats = ifc.getStatistics();
                DbFacade.getInstance().getVmNetworkStatisticsDAO().update(stats);
            }
        }
    }

    public void SetVmDown(VM vm) {
        RemoveAsyncRunningVm(vm.getId());
        InternalSetVmStatus(vm, VMStatus.Down);
        storeVm(vm);

    }

    public boolean IsVmDuringInitiating(Guid vm_guid) {
        return _asyncRunningVms.containsKey(vm_guid);
    }

    /**
     * Set vm status without saving to DB
     *
     * @param vm
     * @param status
     */
    public void InternalSetVmStatus(VM vm, VMStatus status) {
        vm.setstatus(status);
        VMStatus vmStatus = vm.getstatus();
        boolean isVmStatusDown = VM.isStatusDown(vmStatus);

        if (isVmStatusDown || vmStatus == VMStatus.Unknown) {
            resetVmAttributes(vm);

            if (isVmStatusDown) {
                vm.setrun_on_vds(null);
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
        vm.setusage_network_percent(0);
        vm.setelapsed_time(Double.valueOf(0));
        vm.setcpu_sys(new Double(0));
        vm.setcpu_user(new Double(0));
        vm.setusage_cpu_percent(0);
        vm.setusage_mem_percent(0);
        vm.setmigrating_to_vds(null);
        vm.setrun_on_vds_name("");
        vm.setguest_cur_user_name(null);
        vm.setguest_os(null);
        vm.setvm_ip(null);
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        for (VmNetworkInterface ifc : interfaces) {
            NetworkStatistics statistics = ifc.getStatistics();
            statistics.setTransmitDropRate(new Double(0));
            statistics.setTransmitRate(new Double(0));
            statistics.setReceiveRate(new Double(0));
            statistics.setReceiveDropRate(new Double(0));
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

    private static final String VDSCommandPrefix = "VDSCommand";

    private static String GetCommandTypeName(VDSCommandType command) {
        String packageName = command.getPackageName();
        String commandName = String.format("%s.%s%s", packageName, command, VDSCommandPrefix);
        return commandName;
    }

    /**
     * Create the command which needs to run.
     *
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
                    (Class<VDSCommandBase<P>>) java.lang.Class.forName(GetCommandTypeName(commandType));
            Constructor<VDSCommandBase<P>> constructor =
                    ReflectionUtils.findConstructor(type, parameters.getClass());

            if (constructor != null) {
                return constructor.newInstance(new Object[] { parameters });
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                log.debug("CreateCommand failed", e.getCause());
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.debug("CreateCommand failed", e);
        } catch (java.lang.Exception e) {
            log.debug("CreateCommand failed", e);
        }

        return null;
    }

    private <P extends VdsIdVDSCommandParametersBase> FutureVDSCommand createFutureCommand(FutureVDSCommandType commandType,
            P parameters) {
        try {
            Class<FutureVDSCommand> type =
                    (Class<FutureVDSCommand>) java.lang.Class.forName(commandType.getFullyQualifiedClassName());
            Constructor<FutureVDSCommand> constructor =
                    ReflectionUtils.findConstructor(type,
                    parameters.getClass());

            if (constructor != null) {
                return constructor.newInstance(new Object[] { parameters });
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                log.debug("CreateCommand failed", e.getCause());
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
            log.debug("CreateCommand failed", e);
        } catch (java.lang.Exception e) {
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

    /**
     * implement this interface with blank methods just in case no event listener is sent from frontend
     *
     * @param vds
     */

    @Override
    public void vdsNotResponding(VDS vds) {
        log.info("ResourceManager:vdsNotResponding - no event listener defined, nothing done.");
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, boolean saveToDb,
            Guid domainId) {
        log.info("ResourceManager:vdsMaintanance - no event listener defined, nothing done.");

    }

    @Override
    public void vdsMovedToMaintanance(Guid vdsId) {
        log.info("ResourceManager:VdsMovedToMaintanance - no event listener defined, nothing done.");
    }

    @Override
    public void storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        log.info("ResourceManager:StorageDomainOperational - no event listener defined, nothing done.");
    }

    @Override
    public void masterDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        log.info("ResourceManager:MasterDomainNotOperational - no event listener defined, nothing done.");
    }

    @Override
    public void processOnVmStop(Guid vmId) {
        log.info("ResourceManager:ProcessOnVmStop - no event listener defined, nothing done.");
    }

    @Override
    public void vdsUpEvent(Guid vdsId) {
        log.info("ResourceManager:RunDedicatedVm - no event listener defined, nothing done.");
    }

    @Override
    public void processOnClientIpChange(VDS vds, Guid vmId) {
        log.info("ResourceManager:ProcessOnClientIpChange - no event listener defined, nothing done.");
    }

    @Override
    public void processOnCpuFlagsChange(Guid vdsId) {
        log.info("ResourceManager:ProcessOnCpuFlagsChange - no event listener defined, nothing done.");
    }

    @Override
    public void rerun(Guid VmId) {
        log.info("ResourceManager:Rerun - no event listener defined, nothing done.");
    }

    @Override
    public void runningSucceded(Guid vmId) {
        log.info("ResourceManager:RunningSucceded - no event listener defined, nothing done.");
    }

    @Override
    public void processOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port) {
        log.info("ResourceManager:ProcessOnVmPoweringUp - no event listener defined, nothing done.");
    }

    @Override
    public void removeAsyncRunningCommand(Guid vmId) {
        log.info("ResourceManager:RemoveAsyncRunningCommand - no event listener defined, nothing done.");
    }

    @Override
    public void storagePoolUpEvent(storage_pool storagePool, boolean isSpmStarted) {
        log.info("ResourceManager:StoragePoolUpEvent - no event listener defined, nothing done.");
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error, TransactionScopeOption transactionScopeOption) {
        log.info("ResourceManager:StoragePoolStatusChange - no event listener defined, nothing done.");
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            VdcBllErrors error) {
        log.info("ResourceManager:StoragePoolStatusChange - no event listener defined, nothing done.");
    }

    @Override
    public void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status) {
        log.info("ResourceManager:StoragePoolStatusChange - no event listener defined, nothing done.");
    }

    @Override
    public void runFailedAutoStartVM(Guid vmId) {
        log.info("ResourceManager:RunFailedAutoStartVM - no event listener defined, nothing done.");
    }

    @Override
    public boolean restartVds(Guid vdsId) {
        log.info("ResourceManager:RestartVds - no event listener defined, nothing done.");
        return false;
    }

    public void UpdateVdsDomainsData(Guid vdsId, String vdsName, Guid storagePoolId,
            ArrayList<VDSDomainsData> vdsDomainData) {
        IrsBrokerCommand.UpdateVdsDomainsData(vdsId, vdsName, storagePoolId, vdsDomainData);
    }

    public boolean isDomainReportedInProblem(Guid storagePoolId, Guid domainId) {
        return IrsBrokerCommand.isDomainReportedInProblem(storagePoolId, domainId);
    }

    @Override
    public void handleVdsVersion(Guid vdsId) {
        log.info("handleVdsVersion - no event listener defined, nothing done.");
    }
}
