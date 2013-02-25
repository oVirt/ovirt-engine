package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.PowerClientMigrateOnConnectCheckParameters;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StartSpiceVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.Ticketing;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@Stateless(name = "VdsEventListener")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Local(IVdsEventListener.class)
@DependsOn("Backend")
public class VdsEventListener implements IVdsEventListener {

    @Override
    public void vdsMovedToMaintenance(VDS vds) {
        try {
            MaintenanceVdsCommand.ProcessStorageOnVdsInactive(vds);
        } finally {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, true);
        }
    }

    @Override
    public EventResult storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        StorageDomainPoolParametersBase parameters =
                new StorageDomainPoolParametersBase(storageDomainId, storagePoolId);
        parameters.setIsInternal(true);
        parameters.setInactive(true);
        boolean isSucceeded = Backend.getInstance().runInternalAction(VdcActionType.DeactivateStorageDomain,
                parameters,
                ExecutionHandler.createInternalJobContext()).getSucceeded();
        return new EventResult(isSucceeded, EventType.DOMAINNOTOPERATIONAL);
    }

    @Override
    public EventResult masterDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        VdcActionParametersBase parameters = new ReconstructMasterParameters(storagePoolId, storageDomainId, true);
        boolean isSucceeded = Backend.getInstance().runInternalAction(VdcActionType.ReconstructMasterDomain,
                parameters,
                ExecutionHandler.createInternalJobContext()).getSucceeded();
        return new EventResult(isSucceeded, EventType.RECONSTRUCT);
    }

    @Override
    public void processOnVmStop(Guid vmId) {
        VmPoolHandler.ProcessVmPoolOnStopVm(vmId, null);
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, boolean saveToDb,
            Guid domainId) {
        vdsNonOperational(vdsId, reason, logCommand, saveToDb, domainId, null);
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, boolean saveToDb,
            Guid domainId,
            Map<String, String> customLogValues) {
        ExecutionHandler.updateSpecificActionJobCompleted(vdsId, VdcActionType.MaintenanceVds, false);
        SetNonOperationalVdsParameters tempVar =
            new SetNonOperationalVdsParameters(vdsId, reason, customLogValues);
        tempVar.setSaveToDb(saveToDb);
        tempVar.setStorageDomainId(domainId);
        tempVar.setShouldBeLogged(logCommand);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar, ExecutionHandler.createInternalJobContext());
    }

    @Override
    public void vdsNotResponding(final VDS vds) {
        ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                log.infoFormat("ResourceManager::vdsNotResponding entered for Host {0}, {1}",
                        vds.getId(),
                        vds.getHostName());
                Backend.getInstance().runInternalAction(VdcActionType.VdsNotRespondingTreatment,
                        new FenceVdsActionParameters(vds.getId(), FenceActionType.Restart),
                        ExecutionHandler.createInternalJobContext());
            }
        });
    }

    @Override
    public boolean vdsUpEvent(final VDS vds) {
        HostStoragePoolParametersBase params = new HostStoragePoolParametersBase(vds);
        boolean isSucceeded = Backend.getInstance().runInternalAction(VdcActionType.InitVdsOnUp, params).getSucceeded();
        if (isSucceeded) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // migrate vms that its their default vds and failback
                        // is on
                        List<VmStatic> vmsToMigrate =
                                DbFacade.getInstance().getVmStaticDao().getAllWithFailbackByVds(vds.getId());
                        if (vmsToMigrate.size() > 0) {
                            ArrayList<VdcActionParametersBase> vmToServerParametersList =
                                    new ArrayList(LinqUtils
                                            .foreach(vmsToMigrate, new Function<VmStatic, VdcActionParametersBase>() {
                                                @Override
                                                public VdcActionParametersBase eval(VmStatic vm) {
                                                    MigrateVmToServerParameters parameters =
                                                            new MigrateVmToServerParameters(false,
                                                                    vm.getId(),
                                                                    vds.getId());
                                                    parameters.setShouldBeLogged(false);
                                                    return parameters;
                                                }
                                            }));
                            ExecutionContext executionContext = new ExecutionContext();
                            executionContext.setMonitored(true);
                            Backend.getInstance().runInternalMultipleActions(VdcActionType.MigrateVmToServer,
                                    vmToServerParametersList,
                                    executionContext);
                        }

                        // run dedicated vm logic
                        // not passing clientinfo will cause to launch on a VDS
                        // instead of power client. this is a possible use case
                        // to fasten the inital boot, then live migrate to power
                        // client on spice connect.
                        List<VM> vms = DbFacade.getInstance().getVmDao().getAllForDedicatedPowerClientByVds(vds.getId());
                        if (vms.size() != 0) {
                            if (Config
                                    .<Boolean> GetValue(ConfigValues.PowerClientDedicatedVmLaunchOnVdsWhilePowerClientStarts)) {
                                Backend.getInstance().runInternalAction(VdcActionType.RunVmOnDedicatedVds,
                                        new RunVmParams(vms.get(0).getId(), vds.getId()),
                                        ExecutionHandler.createInternalJobContext());
                            } else {
                                ThreadUtils.sleep(10000);
                                Backend.getInstance().runInternalAction(VdcActionType.RunVmOnPowerClient,
                                        new RunVmParams(vms.get(0).getId(), vds.getId()),
                                        ExecutionHandler.createInternalJobContext());
                            }
                        }
                    } catch (RuntimeException e) {
                        log.errorFormat("Failed to initialize Vds on up. Error: {0}", e);
                    }
                }
            });
        }
        return isSucceeded;
    }

    @Override
    public void processOnClientIpChange(final VDS vds, final Guid vmId) {
        final VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        // when a spice client connects to the VM, we need to check if the
        // client is local or remote to adjust compression and migration aspects
        // we first check if we need to disable/enable compression for power
        // clients, so we won't need to handle migration errors
        if (StringUtils.isNotEmpty(vmDynamic.getclient_ip())) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    RunVmCommandBase.doCompressionCheck(vds, vmDynamic);

                    // Run PowerClientMigrateOnConnectCheck if configured.
                    if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateToPowerClientOnConnect)
                            || Config.<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient)) {
                        Backend.getInstance().runInternalAction(VdcActionType.PowerClientMigrateOnConnectCheck,
                                new PowerClientMigrateOnConnectCheckParameters(false,
                                        vmDynamic.getId(),
                                        vmDynamic.getclient_ip(),
                                        vds.getId()),
                                ExecutionHandler.createInternalJobContext());
                    }
                }
            });
        }
        // in case of empty clientIp we clear the logged in user.
        // (this happened when user close the console to spice/vnc)
        else {
            vmDynamic.setConsole_current_user_name(null);
            DbFacade.getInstance().getVmDynamicDao().update(vmDynamic);
        }
    }

    @Override
    public void processOnCpuFlagsChange(Guid vdsId) {
        Backend.getInstance().runInternalAction(VdcActionType.HandleVdsCpuFlagsOrClusterChanged,
                new VdsActionParameters(vdsId));
    }

    @Override
    public void handleVdsVersion(Guid vdsId) {
        Backend.getInstance().runInternalAction(VdcActionType.HandleVdsVersion, new VdsActionParameters(vdsId));
    }

    @Override
    public void processOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmid);

        if (command != null) {
            command.onPowerringUp();
            if (command.getAutoStart() && command.getAutoStartVdsId() != null) {
                try {
                    String otp64 = Ticketing.GenerateOTP();
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(VDSCommandType.SetVmTicket,
                                    new SetVmTicketVDSCommandParameters(vds_id, vmid, otp64, 60, "", Guid.Empty));
                    log.infoFormat(
                            "VdsEventListener.ProcessOnVmPoweringUp - Auto start logic, starting spice to vm - {0} ",
                            vmid);
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.StartSpice,
                                    new StartSpiceVDSCommandParameters(command.getAutoStartVdsId(), display_ip,
                                            display_port, otp64));
                } catch (RuntimeException ex) {
                    log.errorFormat(
                            "VdsEventListener.ProcessOnVmPoweringUp - failed to start spice on VM - {0} - {1} - {2}",
                            vmid,
                            ex.getMessage(),
                            ex.getStackTrace());
                }
            }
        }
    }

    @Override
    public void storagePoolUpEvent(storage_pool storagePool, boolean isNewSpm) {
        if (isNewSpm) {
            AsyncTaskManager.getInstance().StopStoragePoolTasks(storagePool);
        } else {
            AsyncTaskManager.getInstance().AddStoragePoolExistingTasks(storagePool);
        }
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
                                        VdcBllErrors error) {
        storagePoolStatusChange(storagePoolId, status, auditLogType, error, null);
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
                                        VdcBllErrors error, TransactionScopeOption transactionScopeOption) {
        SetStoragePoolStatusParameters tempVar =
                new SetStoragePoolStatusParameters(storagePoolId, status, auditLogType);
        tempVar.setError(error);
        if (transactionScopeOption != null) {
            tempVar.setTransactionScopeOption(transactionScopeOption);
        }
        Backend.getInstance().runInternalAction(VdcActionType.SetStoragePoolStatus, tempVar);
    }

    @Override
    public void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status) {
        StoragePoolStatusHandler.PoolStatusChanged(storagePoolId, status);
    }

    @Override
    public void runFailedAutoStartVM(Guid vmId) {
        // We will reuse this because we can generate more than one event:
        final AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);

        // Alert that the virtual machine failed:
        AuditLogDirector.log(event, AuditLogType.HA_VM_FAILED);
        log.infoFormat("Failed to start Highly Available VM. Attempting to restart. VM Name: {0}, VM Id:{1}",
                event.getVmName(), vmId);

        // Try to start it again:
        final VdcReturnValueBase result = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext());

        // Alert if the restart fails:
        if (!result.getSucceeded()) {
                AuditLogDirector.log(event, AuditLogType.HA_VM_RESTART_FAILED);
        }
    }

    @Override
    public boolean restartVds(Guid vdsId) {
        return Backend
                .getInstance()
                .runInternalAction(VdcActionType.RestartVds,
                        new FenceVdsActionParameters(vdsId, FenceActionType.Restart),
                        ExecutionHandler.createInternalJobContext())
                .getSucceeded();
    }

    @Override
    public void rerun(Guid vmId) {
        final IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmId);
        if (command != null) {
            // The command will be invoked in a different VDS in its rerun method, so we're calling
            // its rerun method from a new thread so that it won't be executed within our current VDSM lock
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    command.rerun();
                }
            });
        }
    }

    @Override
    public void runningSucceded(Guid vmId) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmId);
        if (command != null) {
            command.runningSucceded();
        }
    }

    @Override
    public void removeAsyncRunningCommand(Guid vmId) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().RemoveAsyncRunningCommand(vmId);
        if (command != null) {
            command.reportCompleted();
        }
    }

    private static Log log = LogFactory.getLog(VdsEventListener.class);
}
