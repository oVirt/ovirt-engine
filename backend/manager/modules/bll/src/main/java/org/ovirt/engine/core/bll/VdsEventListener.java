package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.AvailableUpdatesFinder;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.qualifiers.MomPolicyUpdate;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterServiceVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VdsEventListener implements IVdsEventListener {

    @Inject
    Instance<ResourceManager> resourceManagerProvider;
    @Inject
    EventQueue eventQueue;
    @Inject
    private AvailableUpdatesFinder availableUpdatesFinder;
    @Inject
    AutoStartVmsRunner autoStartVmsRunner;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private BackendInternal backend;
    @Inject
    private Instance<HostedEngineImporter> hostedEngineImporterProvider;
    @Inject
    private SchedulingManager schedulingManager;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private GlusterBrickDao glusterBrickDao;
    @Inject
    private VDSBrokerFrontend vdsBroker;

    private static final Logger log = LoggerFactory.getLogger(VdsEventListener.class);

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
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString())), null);
            try {
                LockManagerFactory.getLockManager().acquireLockWait(lock);
                clearDomainCache(vds);
                stopGlusterServices(vds);
                StoragePool storage_pool = storagePoolDao.get(vds.getStoragePoolId());
                if (StoragePoolStatus.Uninitialized != storage_pool
                        .getStatus()) {
                    vdsBroker.RunVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(vds.getId(),
                                        vds.getStoragePoolId(), vds.getVdsSpmId()));
                    HostStoragePoolParametersBase params =
                            new HostStoragePoolParametersBase(storage_pool, vds);
                    backend.runInternalAction(VdcActionType.DisconnectHostFromStoragePoolServers, params);
                }
            } finally {
                LockManagerFactory.getLockManager().releaseLock(lock);
            }
        }
    }

    private void stopGlusterServices(VDS vds) {
        if (vds.getVdsGroupSupportsGlusterService()) {
            // Stop glusterd service first
            boolean succeeded = resourceManagerProvider.get().runVdsCommand(VDSCommandType.ManageGlusterService,
                    new GlusterServiceVDSParameters(vds.getId(), Arrays.asList("glusterd"), "stop")).getSucceeded();
            if (succeeded) {
                // Stop other gluster related processes on the node
                succeeded = resourceManagerProvider.get().runVdsCommand(VDSCommandType.StopGlusterProcesses,
                        new VdsIdVDSCommandParametersBase(vds.getId())).getSucceeded();
                // Mark the bricks as DOWN on this node
                if (succeeded) {
                    List<GlusterBrickEntity> bricks =
                            glusterBrickDao.getGlusterVolumeBricksByServerId(vds.getId());
                    for (GlusterBrickEntity brick : bricks) {
                        brick.setStatus(GlusterStatus.DOWN);
                    }
                    glusterBrickDao.updateBrickStatuses(bricks);
                }
            }
            if(!succeeded){
                log.error("Failed to stop gluster services while moving the host '{}' to maintenance", vds.getName());
            }
        }
    }

    /**
     * The following method will clear a cache for problematic domains, which were reported by vds
     *
     * @param vds
     */
    private void clearDomainCache(final VDS vds) {
        eventQueue.submitEventSync(new Event(vds.getStoragePoolId(),
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
        boolean isSucceeded = backend.runInternalAction(VdcActionType.DeactivateStorageDomain,
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
        boolean isSucceeded = backend.runInternalAction(VdcActionType.ReconstructMasterDomain,
                parameters,
                ExecutionHandler.createInternalJobContext()).getSucceeded();
        return new EventResult(isSucceeded, EventType.RECONSTRUCT);
    }

    @Override
    public void processOnVmStop(final Collection<Guid> vmIds, final Guid hostId) {
        processOnVmStop(vmIds, hostId, true);
    }

    @Override
    public void processOnVmStop(final Collection<Guid> vmIds, final Guid hostId, boolean useSeparateThread) {
        if (vmIds.isEmpty()) {
            return;
        }

        if (useSeparateThread) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    processOnVmStopInternal(vmIds, hostId);
                }
            });
        } else {
            processOnVmStopInternal(vmIds, hostId);
        }
    }

    private void processOnVmStopInternal(final Collection<Guid> vmIds, final Guid hostId) {
        for (Guid vmId : vmIds) {
            backend.runInternalAction(VdcActionType.ProcessDownVm,
                    new ProcessDownVmParameters(vmId, true));
        }

        HostDeviceManager hostDeviceManager = Injector.get(HostDeviceManager.class);
        hostDeviceManager.refreshHostIfAnyVmHasHostDevices(vmIds, hostId);
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
                backend.runInternalAction(VdcActionType.SyncLunsInfoForBlockStorageDomain, parameters);
            }
        });
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId) {
        StorageDomainStatic storageDomain = DbFacade.getInstance().getStorageDomainStaticDao().get(domainId);
        Map<String, String> customLogValues = null;
        if (storageDomain != null) {
            customLogValues = Collections.singletonMap("StorageDomainNames", storageDomain.getName());
        }
        vdsNonOperational(vdsId, reason, logCommand, domainId, customLogValues);
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId,
            Map<String, String> customLogValues) {
        ExecutionHandler.updateSpecificActionJobCompleted(vdsId, VdcActionType.MaintenanceVds, false);
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(vdsId, reason, customLogValues);
        tempVar.setStorageDomainId(domainId);
        tempVar.setShouldBeLogged(logCommand);
        backend.runInternalAction(VdcActionType.SetNonOperationalVds,
                tempVar,
                ExecutionHandler.createInternalJobContext());
    }

    @Override
    public void vdsNotResponding(final VDS vds) {
        ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                log.info("ResourceManager::vdsNotResponding entered for Host '{}', '{}'",
                        vds.getId(),
                        vds.getHostName());

                FenceVdsActionParameters params = new FenceVdsActionParameters(vds.getId());
                backend.runInternalAction(VdcActionType.VdsNotRespondingTreatment,
                        params,
                        ExecutionHandler.createInternalJobContext());

                moveBricksToUnknown(vds);
            }
        });
    }

    private void moveBricksToUnknown(final VDS vds) {
        List<GlusterBrickEntity> brickEntities =
                DbFacade.getInstance().getGlusterBrickDao().getGlusterVolumeBricksByServerId(vds.getId());
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
        boolean isSucceeded = backend.runInternalAction(VdcActionType.InitVdsOnUp, params).getSucceeded();
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
                            backend.runInternalMultipleActions(VdcActionType.MigrateVmToServer,
                                    new ArrayList<>(createMigrateVmToServerParametersList(vmsToMigrate, vds)),
                                    ctx);
                        }
                    } catch (RuntimeException e) {
                        log.error("Failed to initialize Vds on up: {}", e.getMessage());
                        log.error("Exception", e);
                    }
                }
            });
        }
        return isSucceeded;
    }

    @Override
    public boolean connectHostToDomainsInActiveOrUnknownStatus(VDS vds) {
        StoragePool sp = storagePoolDao.get(vds.getStoragePoolId());
        ConnectHostToStoragePoolServersParameters params =
                new ConnectHostToStoragePoolServersParameters(sp, vds, false);
        return backend
                .runInternalAction(VdcActionType.ConnectHostToStoragePoolServers, params)
                .getSucceeded();
    }

    private List<VdcActionParametersBase> createMigrateVmToServerParametersList(List<VmStatic> vmsToMigrate,
            final VDS vds) {
        return LinqUtils.transformToList(vmsToMigrate,
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
    public void processOnClientIpChange(final Guid vmId, String newClientIp) {
        final AuditLogableBase event = new AuditLogableBase();
        final VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(vmId);
        event.setVmId(vmId);
        event.setUserName(vmDynamic.getConsoleCurrentUserName());

        // in case of empty clientIp we clear the logged in user.
        // (this happened when user close the console to spice/vnc)
        if (StringUtils.isEmpty(newClientIp)) {
            vmDynamic.setConsoleCurrentUserName(null);
            DbFacade.getInstance().getVmDynamicDao().update(vmDynamic);
            auditLogDirector.log(event, AuditLogType.VM_CONSOLE_DISCONNECTED);
        } else {
            auditLogDirector.log(event, AuditLogType.VM_CONSOLE_CONNECTED);
        }
    }

    @Override
    public void processOnCpuFlagsChange(Guid vdsId) {
        backend.runInternalAction(VdcActionType.HandleVdsCpuFlagsOrClusterChanged,
                new VdsActionParameters(vdsId));
    }

    @Override
    public void handleVdsVersion(Guid vdsId) {
        backend.runInternalAction(VdcActionType.HandleVdsVersion, new VdsActionParameters(vdsId));
    }

    @Override
    public void processOnVmPoweringUp(Guid vmId) {
        IVdsAsyncCommand command = vdsBroker.GetAsyncCommandForVm(vmId);

        if (command != null) {
            command.onPowerringUp();
        }
    }

    @Override
    public void storagePoolUpEvent(StoragePool storagePool) {
        CommandCoordinatorUtil.addStoragePoolExistingTasks(storagePool);
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            EngineError error) {
        storagePoolStatusChange(storagePoolId, status, auditLogType, error, null);
    }

    @Override
    public void storagePoolStatusChange(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType,
            EngineError error, TransactionScopeOption transactionScopeOption) {
        SetStoragePoolStatusParameters tempVar =
                new SetStoragePoolStatusParameters(storagePoolId, status, auditLogType);
        tempVar.setError(error);
        if (transactionScopeOption != null) {
            tempVar.setTransactionScopeOption(transactionScopeOption);
        }
        backend.runInternalAction(VdcActionType.SetStoragePoolStatus, tempVar);
    }

    @Override
    public void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status) {
        StoragePoolStatusHandler.poolStatusChanged(storagePoolId, status);
    }

    @Override
    public void runFailedAutoStartVMs(List<Guid> vmIds) {
        for (Guid vmId : vmIds) {
            // Alert that the virtual machine failed:
            AuditLogableBase event = new AuditLogableBase();
            event.setVmId(vmId);
            auditLogDirector.log(event, AuditLogType.HA_VM_FAILED);

            log.info("Highly Available VM went down. Attempting to restart. VM Name '{}', VM Id '{}'",
                    event.getVmName(), vmId);
        }

        autoStartVmsRunner.addVmsToRun(vmIds);
    }

    @Override
    public void addExternallyManagedVms(List<VmStatic> externalVmList) {
        for (VmStatic currVm : externalVmList) {
            AddVmParameters params = new AddVmParameters(currVm);
            VdcReturnValueBase returnValue =
                    backend.runInternalAction(VdcActionType.AddVmFromScratch,
                            params,
                            ExecutionHandler.createInternalJobContext());
            if (!returnValue.getSucceeded()) {
                log.debug("Failed adding Externally managed VM '{}'", currVm.getName());
            }
        }
    }

    @Override
    public void handleVdsMaintenanceTimeout(Guid vdsId) {
        // try to put the host to Maintenance again.
        backend.runInternalAction(VdcActionType.MaintenanceNumberOfVdss,
                new MaintenanceNumberOfVdssParameters(Arrays.asList(vdsId), true));
    }

    @Override
    public void rerun(Guid vmId) {
        final IVdsAsyncCommand command = vdsBroker.GetAsyncCommandForVm(vmId);
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
        IVdsAsyncCommand command = vdsBroker.GetAsyncCommandForVm(vmId);
        if (command != null) {
            command.runningSucceded();
        }
    }

    @Override
    public void removeAsyncRunningCommand(Guid vmId) {
        IVdsAsyncCommand command = vdsBroker.RemoveAsyncRunningCommand(vmId);
        if (command != null) {
            command.reportCompleted();
        }
    }

    @Override
    public void updateSchedulingStats(VDS vds) {
        schedulingManager.updateHostSchedulingStats(vds);
    }

    @Override
    public void updateSlaPolicies(final List<Guid> vmIds, final Guid vdsId) {
        if (vmIds.isEmpty()) {
            return;
        }
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                for (Guid vmId : vmIds) {
                    CpuQos qos = DbFacade.getInstance().getCpuQosDao().getCpuQosByVmId(vmId);
                    if (qos != null && qos.getCpuLimit() != null) {
                        resourceManagerProvider.get().runVdsCommand(VDSCommandType.UpdateVmPolicy,
                                new UpdateVmPolicyVDSParams(vdsId, vmId, qos.getCpuLimit().intValue()));
                    }
                }
            }
        });
    }

    public void onError(@Observes final VDSNetworkException vdsException) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                resourceManagerProvider.get().getVdsManager(
                        vdsException.getVdsError().getVdsId()).handleNetworkException(vdsException);
            }
        });
    }

    @Override
    public void refreshHostIfAnyVmHasHostDevices(final List<Guid> vmIds, final Guid hostId) {
        if (vmIds.isEmpty()) {
            return;
        }

        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                HostDeviceManager hostDeviceManager = Injector.get(HostDeviceManager.class);
                hostDeviceManager.refreshHostIfAnyVmHasHostDevices(vmIds, hostId);
            }
        });
    }

    public boolean isUpdateAvailable(VDS host) {
        return availableUpdatesFinder.isUpdateAvailable(host);
    }

    // TODO asynch event handler - design infra code to allow async events in segregated thread
    public void onMomPolicyChange(@Observes @MomPolicyUpdate final VDSGroup cluster) {
        if (cluster == null || cluster.getCompatibilityVersion().compareTo(Version.v3_4) < 0)
            return;
        List<VDS> activeHostsInCluster =
                vdsDao.getAllForVdsGroupWithStatus(cluster.getId(), VDSStatus.Up);
        // collect all Active hosts into a callable list
        List<Callable<Object>> callables = new LinkedList<>();
        for (final VDS vds : activeHostsInCluster) {
            callables.add(new Callable<Object>() {
                @Override
                public Object call() {
                    try {
                        resourceManagerProvider.get().runVdsCommand(VDSCommandType.SetMOMPolicyParameters,
                                new MomPolicyVDSParameters(vds,
                                        cluster.isEnableBallooning(),
                                        cluster.isEnableKsm(),
                                        cluster.isKsmMergeAcrossNumaNodes())
                                );
                    } catch (EngineException e) {
                        log.error("Could not update MoM policy on host '{}'", vds.getName());
                    }
                    return null;
                }
            });
        }
        // run all VDSCommands concurrently with executor
        if (callables.size() > 0)
            ThreadPoolUtil.invokeAll(callables);
    }

    @Override
    public void importHostedEngineVm(final VM vm) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override public void run() {
                hostedEngineImporterProvider.get().doImport(vm);
            }
        });
    }
}
