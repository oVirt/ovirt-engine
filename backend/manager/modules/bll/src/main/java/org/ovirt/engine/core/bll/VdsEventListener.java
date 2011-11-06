package org.ovirt.engine.core.bll;

import java.util.List;

import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.PowerClientMigrateOnConnectCheckParameters;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
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
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StartSpiceVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.Helper;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.Ticketing;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@Stateless(name = "VdsEventListener")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Local(IVdsEventListener.class)
@DependsOn("Backend")
public class VdsEventListener implements IVdsEventListener {

    @Override
    public void VdsMovedToMaintanance(Guid vdsId) {
        VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
        MaintananceVdsCommand.ProcessStorageOnVdsInactive(vds);
    }

    @Override
    public void StorageDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        Backend.getInstance().runInternalAction(VdcActionType.HandleFailedStorageDomain,
                new StorageDomainPoolParametersBase(storageDomainId, storagePoolId));
    }

    @Override
    public void MasterDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        VdcActionParametersBase parameters = new ReconstructMasterParameters(storagePoolId, storageDomainId, false);
        parameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        Backend.getInstance().runInternalAction(VdcActionType.ReconstructMasterDomain, parameters);
    }

    @Override
    public void ProcessOnVmStop(Guid vmId) {
        VmPoolHandler.ProcessVmPoolOnStopVm(vmId);
        /**
         * Vitaly wating for Vm.ExitStatus in DB.
         * //HighAvailableVmsDirector.TryRunHighAvailableVmsOnVmDown(vmId);
         */
    }

    @Override
    public void VdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, boolean saveToDb,
                                  Guid domainId) {
        SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(vdsId, reason);
        tempVar.setSaveToDb(saveToDb);
        tempVar.setStorageDomainId(domainId);
        tempVar.setShouldBeLogged(logCommand);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar);
    }

    @Override
    public void VdsNotResponding(final VDS vds) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                log.infoFormat("ResourceManager::vdsNotResponding entered for Host {0}, {1}",
                        vds.getvds_id(),
                        vds.gethost_name());
                Backend.getInstance().runInternalAction(VdcActionType.VdsNotRespondingTreatment,
                        new FenceVdsActionParameters(vds.getvds_id(), FenceActionType.Restart));
            }
        });
    }

    @Override
    public void VdsUpEvent(final Guid vdsId) {
        StoragePoolParametersBase tempVar = new StoragePoolParametersBase(Guid.Empty);
        tempVar.setVdsId(vdsId);
        if (Backend.getInstance().runInternalAction(VdcActionType.InitVdsOnUp, tempVar).getSucceeded()) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // migrate vms that its their default vds and failback
                        // is on
                        List<VmStatic> vmsToMigrate =
                                DbFacade.getInstance().getVmStaticDAO().getAllWithFailbackByVds(vdsId);
                        java.util.ArrayList<VdcActionParametersBase> vmToServerParametersList = Helper.ToList(LinqUtils
                                .foreach(vmsToMigrate, new Function<VmStatic, VdcActionParametersBase>() {
                                    @Override
                                    public VdcActionParametersBase eval(VmStatic vm) {
                                        MigrateVmToServerParameters parameters =
                                            new MigrateVmToServerParameters(false, vm.getId(), vdsId);
                                        parameters.setShouldBeLogged(false);
                                        return parameters;
                                    }
                                }));
                        Backend.getInstance().runInternalMultipleActions(VdcActionType.MigrateVmToServer,
                                vmToServerParametersList);

                        // LINQ 29456
                        // Backend.getInstance().RunMultipleActions(VdcActionType.MigrateVmToServer,
                        // vmsToMigrate.Select<VmStatic,
                        // VdcActionParametersBase>(
                        // a => new MigrateVmToServerParameters(a.vm_guid,
                        // vdsId)).ToList());

                        // run dedicated vm logic
                        // not passing clientinfo will cause to launch on a VDS
                        // instead of power client. this is a possible use case
                        // to fasten the inital boot, then live migrate to power
                        // client on spice connect.
                        /**
                         * Vitaly wating for Vm.ExitStatus in DB.
                         * //HighAvailableVmsDirector
                         * .TryRunHighAvailableVdsUp(vdsId);
                         */
                        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllForDedicatedPowerClientByVds(vdsId);
                        if (vms.size() != 0) {
                            if (Config
                                    .<Boolean> GetValue(ConfigValues.PowerClientDedicatedVmLaunchOnVdsWhilePowerClientStarts)) {
                                Backend.getInstance().runInternalAction(VdcActionType.RunVmOnDedicatedVds,
                                        new RunVmParams(vms.get(0).getvm_guid(), vdsId));
                            } else {
                                ThreadUtils.sleep(10000);
                                Backend.getInstance().runInternalAction(VdcActionType.RunVmOnPowerClient,
                                        new RunVmParams(vms.get(0).getvm_guid(), vdsId));
                            }
                        }
                    } catch (RuntimeException e) {
                        log.errorFormat("Failed to initialize Vds on up. Error: {0}", e);
                    }
                }
            });
        }
    }

    @Override
    public void ProcessOnClientIpChange(final VDS vds, final Guid vmId) {
        final VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDAO().get(vmId);
        // when a spice client connects to the VM, we need to check if the
        // client is local or remote to adjust compression and migration aspects
        // we first check if we need to disable/enable compression for power
        // clients, so we won't need to handle migration errors
        if (!StringHelper.isNullOrEmpty(vmDynamic.getclient_ip())) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    RunVmCommandBase.DoCompressionCheck(vds, vmDynamic);

                    // Run PowerClientMigrateOnConnectCheck if configured.
                    if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateToPowerClientOnConnect)
                            || Config.<Boolean> GetValue(ConfigValues.PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient)) {
                        Backend.getInstance().runInternalAction(
                                VdcActionType.PowerClientMigrateOnConnectCheck,
                                              new PowerClientMigrateOnConnectCheckParameters(false,
                                                                                             vmDynamic.getId(),
                                                                                             vmDynamic
                                                                                                     .getclient_ip(),
                                                      vds.getvds_id()));
                    }
                }
            });
        }
        // in case of empty clientIp we clear the logged in user.
        // (this happened when user close the console to spice/vnc)
        else {
            vmDynamic.setguest_cur_user_id(null);
            vmDynamic.setguest_cur_user_name(null);
            DbFacade.getInstance().getVmDynamicDAO().update(vmDynamic);
        }
    }

    @Override
    public void ProcessOnCpuFlagsChange(Guid vdsId) {
        Backend.getInstance().runInternalAction(VdcActionType.HandleVdsCpuFlagsOrClusterChanged,
                new VdsActionParameters(vdsId));
        Backend.getInstance().runInternalAction(VdcActionType.HandleVdsVersion, new VdsActionParameters(vdsId));
    }

    @Override
    public void ProcessOnVmPoweringUp(Guid vds_id, Guid vmid, String display_ip, int display_port) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmid);
        if (command != null && command.getAutoStart() && command.getAutoStartVdsId() != null) {
            try {
                String otp64 = Ticketing.GenerateOTP();
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SetVmTicket,
                                new SetVmTicketVDSCommandParameters(vds_id, vmid, otp64, 60));
                log.infoFormat(
                        "VdsEventListener.ProcessOnVmPoweringUp - Auto start logic, starting spice to vm - {0} ", vmid);
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.StartSpice,
                                new StartSpiceVDSCommandParameters(command.getAutoStartVdsId(), display_ip,
                                        display_port, otp64));
            } catch (RuntimeException ex) {
                log.errorFormat(
                        "VdsEventListener.ProcessOnVmPoweringUp - failed to start spice on VM - {0} - {1} - {2}", vmid,
                        ex.getMessage(), ex.getStackTrace());
            }
        }
    }

    @Override
    public void StoragePoolUpEvent(storage_pool storagePool, boolean isNewSpm) {
        if (isNewSpm) {
            AsyncTaskManager.getInstance().StopStoragePoolTasks(storagePool);
        }
        {
            AsyncTaskManager.getInstance().AddStoragePoolExistingTasks(storagePool);
        }
    }

    @Override
    public void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
                                        VdcBllErrors error) {
        StoragePoolStatusChange(storagePoolId, status, auditLogType, error, null);
    }

    @Override
    public void StoragePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
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
    public void StoragePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status) {
        StoragePoolStatusHandler.PoolStatusChanged(storagePoolId, status);
    }

    @Override
    public void RunFailedAutoStartVM(Guid vmId) {
        Backend.getInstance().runInternalAction(VdcActionType.RunVm, new RunVmParams(vmId));
    }

    @Override
    public boolean RestartVds(Guid vdsId) {
        return Backend
                .getInstance()
                .runInternalAction(VdcActionType.RestartVds,
                        new FenceVdsActionParameters(vdsId, FenceActionType.Restart)).getSucceeded();
    }

    @Override
    public void Rerun(Guid vmId) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmId);
        if (command != null) {
            command.Rerun();
        }
    }

    @Override
    public void RunningSucceded(Guid vmId) {
        IVdsAsyncCommand command = Backend.getInstance().getResourceManager().GetAsyncCommandForVm(vmId);
        if (command != null) {
            command.RunningSucceded();
        }
    }

    @Override
    public void RemoveAsyncRunningCommand(Guid vmId) {
        Backend.getInstance().getResourceManager().RemoveAsyncRunningCommand(vmId);
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsEventListener.class);
}
