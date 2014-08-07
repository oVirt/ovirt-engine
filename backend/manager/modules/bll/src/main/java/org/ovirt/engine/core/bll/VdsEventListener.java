package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StartSpiceVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.Ticketing;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.MonitoringStrategyFactory;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

@Stateless(name = "VdsEventListener")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Local(IVdsEventListener.class)
@DependsOn("Backend")
public class VdsEventListener implements IVdsEventListener {

    @Override
    public void vdsMovedToMaintenance(VDS vds) {
        try {
            processStorageOnVdsInactive(vds);
        } finally {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, true);
        }
    }


    private void processStorageOnVdsInactive(final VDS vds) {

        // Clear the problematic timers since the VDS is in maintenance so it doesn't make sense to check it
        // anymore.
        if (!Guid.Empty.equals(vds.getStoragePoolId())) {
            // when vds is being moved to maintenance, this is the part in which we disconnect it from the pool
            // and the storage server. it should be synced with the host autorecovery mechanism to try to avoid
            // leaving the host with storage/pool connection when it's on maintenance.
            EngineLock lock = new EngineLock(Collections.singletonMap(vds.getId().toString(),
                    new Pair<>(LockingGroup.VDS_POOL_AND_STORAGE_CONNECTIONS.toString(),
                            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString())), null);
            try {
                LockManagerFactory.getLockManager().acquireLockWait(lock);
                clearDomainCache(vds);

                StoragePool storage_pool = DbFacade.getInstance()
                        .getStoragePoolDao()
                        .get(vds.getStoragePoolId());
                if (StoragePoolStatus.Uninitialized != storage_pool
                        .getStatus()) {
                    Backend.getInstance().getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.DisconnectStoragePool,
                                    new DisconnectStoragePoolVDSCommandParameters(vds.getId(),
                                            vds.getStoragePoolId(), vds.getVdsSpmId()));
                    HostStoragePoolParametersBase params =
                            new HostStoragePoolParametersBase(storage_pool, vds);
                    Backend.getInstance().runInternalAction(VdcActionType.DisconnectHostFromStoragePoolServers, params);
                }
            } finally {
                LockManagerFactory.getLockManager().releaseLock(lock);
            }
        }
    }

    /**
     * The following method will clear a cache for problematic domains, which were reported by vds
     * @param vds
     */
    private void clearDomainCache(final VDS vds) {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(vds.getStoragePoolId(),
                null, vds.getId(), EventType.VDSCLEARCACHE, ""),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        IrsBrokerCommand.clearVdsFromCache(vds.getStoragePoolId(), vds.getId(), vds.getName());
                        return new EventResult(true, EventType.VDSCLEARCACHE);
                    }
                });
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
    public EventResult masterDomainNotOperational(Guid storageDomainId,
            Guid storagePoolId,
            boolean isReconstructToInactiveDomains,
            boolean canReconstructToCurrentMaster) {
        VdcActionParametersBase parameters =
                new ReconstructMasterParameters(storagePoolId,
                        storageDomainId,
                        true,
                        isReconstructToInactiveDomains,
                        canReconstructToCurrentMaster);
        boolean isSucceeded = Backend.getInstance().runInternalAction(VdcActionType.ReconstructMasterDomain,
                parameters,
                ExecutionHandler.createInternalJobContext()).getSucceeded();
        return new EventResult(isSucceeded, EventType.RECONSTRUCT);
    }

    @Override
    public void processOnVmStop(final Collection<Guid> vmIds) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                for (Guid vmId : vmIds) {
                    Backend.getInstance().runInternalAction(VdcActionType.ProcessDownVm,
                            new IdParameters(vmId));
                }
            }
        });
    }

    /**
     * Synchronize LUN details comprising the storage domain with the DB
     *
     * @param storageDomainId
     * @param vdsId
     */
    @Override
    public void syncLunsInfoForBlockStorageDomain(final Guid storageDomainId, final Guid vdsId) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                StorageDomainParametersBase parameters = new StorageDomainParametersBase(storageDomainId);
                parameters.setVdsId(vdsId);
                Backend.getInstance().runInternalAction(VdcActionType.SyncLunsInfoForBlockStorageDomain, parameters);
            }
        });
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId) {
        vdsNonOperational(vdsId, reason, logCommand, domainId, null);
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId,
            Map<String, String> customLogValues) {
        ExecutionHandler.updateSpecificActionJobCompleted(vdsId, VdcActionType.MaintenanceVds, false);
        SetNonOperationalVdsParameters tempVar =
            new SetNonOperationalVdsParameters(vdsId, reason, customLogValues);
        tempVar.setStorageDomainId(domainId);
        tempVar.setShouldBeLogged(logCommand);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar, ExecutionHandler.createInternalJobContext());
    }

    @Override
    public void vdsNotResponding(final VDS vds, final boolean executeSshSoftFencing, final long lastUpdate) {
        ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                log.infoFormat("ResourceManager::vdsNotResponding entered for Host {0}, {1}",
                        vds.getId(),
                        vds.getHostName());

                boolean shouldExecRealFencing = true;

                if (executeSshSoftFencing) {
                    VdcReturnValueBase retVal =
                            Backend.getInstance().runInternalAction(VdcActionType.SshSoftFencing,
                                    new VdsActionParameters(vds.getId()),
                                    ExecutionHandler.createInternalJobContext());
                    // if SSH Soft Fencing command execution was successful, do not execute
                    // standard fencing immediately, but wait to see if it helped
                    shouldExecRealFencing = !retVal.getSucceeded();
                }

                if (MonitoringStrategyFactory.getMonitoringStrategyForVds(vds).isPowerManagementSupported()
                        && shouldExecRealFencing) {
                    FenceVdsActionParameters params = new FenceVdsActionParameters(
                            vds.getId(),
                            FenceActionType.Restart);
                    params.setLastUpdate(lastUpdate);
                    Backend.getInstance().runInternalAction(VdcActionType.VdsNotRespondingTreatment,
                            params,
                            ExecutionHandler.createInternalJobContext());
                }
                moveBricksToUnknown(vds);
            }
        });
    }

    private void moveBricksToUnknown(final VDS vds) {
        List<GlusterBrickEntity> brickEntities = DbFacade.getInstance().getGlusterBrickDao().getGlusterVolumeBricksByServerId(vds.getId());
        for (GlusterBrickEntity brick : brickEntities) {
            if (brick.getStatus() == GlusterStatus.UP) {
                brick.setStatus(GlusterStatus.UNKNOWN);
            }
        }
        DbFacade.getInstance().getGlusterBrickDao().updateBrickStatuses(brickEntities);
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
                        if (!vmsToMigrate.isEmpty()) {
                            CommandContext ctx = new CommandContext(new EngineContext());
                            ctx.getExecutionContext().setMonitored(true);
                            Backend.getInstance().runInternalMultipleActions(VdcActionType.MigrateVmToServer,
                                    new ArrayList<>(createMigrateVmToServerParametersList(vmsToMigrate, vds)),
                                    ctx);
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
    public boolean connectHostToDomainsInActiveOrUnknownStatus(VDS vds) {
        ConnectHostToStoragePoolServersParameters params = new ConnectHostToStoragePoolServersParameters(vds, false);
        return Backend.getInstance().runInternalAction(VdcActionType.ConnectHostToStoragePoolServers, params).getSucceeded();
    }


    private List<VdcActionParametersBase> createMigrateVmToServerParametersList(List<VmStatic> vmsToMigrate, final VDS vds) {
        return LinqUtils.foreach(vmsToMigrate,
                new Function<VmStatic, VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase eval(VmStatic vm) {
                MigrateVmToServerParameters parameters =
                        new MigrateVmToServerParameters(false,
                                vm.getId(),
                                vds.getId());
                parameters.setShouldBeLogged(false);
                return parameters;
            }
        });
    }

    @Override
    public void processOnClientIpChange(final VDS vds, final Guid vmId) {
        final VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        final AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);
        String username = vmDynamic.getConsoleCurrentUserName();
        if(username!= null && username.contains("@")){
            username = username.substring(0, username.indexOf("@"));
        }
        event.setUserName(username);

        // in case of empty clientIp we clear the logged in user.
        // (this happened when user close the console to spice/vnc)
        if (StringUtils.isEmpty(vmDynamic.getClientIp())) {
            vmDynamic.setConsoleCurrentUserName(null);
            DbFacade.getInstance().getVmDynamicDao().update(vmDynamic);
            AuditLogDirector.log(event, AuditLogType.VM_CONSOLE_DISCONNECTED);
        } else {
            AuditLogDirector.log(event, AuditLogType.VM_CONSOLE_CONNECTED);
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

        /*
         * XXX: command is null after successful migration, because runningSucceeded removes the
         *      MigrateVmCommand instance from the async cache too early.
         *      See the order of succeededToRunVm and processOnVmPoweringUp
         *      in VdsUpdateRunTimeInfo.afterRefreshTreatment
         */
        if (command != null) {
            command.onPowerringUp();
            if (command.getAutoStart() && command.getAutoStartVdsId() != null) {
                try {
                    String otp64 = Ticketing.generateOTP();
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
    public void storagePoolUpEvent(StoragePool storagePool) {
        CommandCoordinatorUtil.addStoragePoolExistingTasks(storagePool);
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
        StoragePoolStatusHandler.poolStatusChanged(storagePoolId, status);
    }

    @Override
    public void runFailedAutoStartVMs(List<Guid> vmIds) {
        for (Guid vmId: vmIds) {
            // Alert that the virtual machine failed:
            AuditLogableBase event = new AuditLogableBase();
            event.setVmId(vmId);
            AuditLogDirector.log(event, AuditLogType.HA_VM_FAILED);

            log.infoFormat("Highly Available VM went down. Attempting to restart. VM Name: {0}, VM Id: {1}",
                    event.getVmName(), vmId);
        }

        AutoStartVmsRunner.getInstance().addVmsToRun(vmIds);
    }

    @Override
    public void addExternallyManagedVms(List<VmStatic> externalVmList) {
        for (VmStatic currVm : externalVmList) {
            AddVmFromScratchParameters params = new AddVmFromScratchParameters(currVm, null, null);
            VdcReturnValueBase returnValue = Backend.getInstance().runInternalAction(VdcActionType.AddVmFromScratch, params, ExecutionHandler.createInternalJobContext());
            if (!returnValue.getSucceeded()) {
                log.debugFormat("Failed adding Externally managed VM {0}", currVm.getName());
            }
        }
    }

    @Override
    public void handleVdsMaintenanceTimeout(final VDS vds) {
        // try to put the host to Maintenance again.
        Backend.getInstance().runInternalAction(VdcActionType.MaintenanceNumberOfVdss,
                new MaintenanceNumberOfVdssParameters(Arrays.asList(vds.getId()), true));
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

    @Override
    public void updateSchedulingStats(VDS vds) {
        SchedulingManager.getInstance().updateHostSchedulingStats(vds);
    }

    private static final Log log = LogFactory.getLog(VdsEventListener.class);

}
