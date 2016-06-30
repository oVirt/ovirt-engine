package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.AvailableUpdatesFinder;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
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
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.IVdsAsyncCommand;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
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
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.monitoring.VmJobsMonitoring;
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
    private HaAutoStartVmsRunner haAutoStartVmsRunner;
    @Inject
    private ColdRebootAutoStartVmsRunner coldRebootAutoStartVmsRunner;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private CpuQosDao cpuQosDao;
    @Inject
    private StorageQosDao storageQosDao;
    @Inject
    private DiskDao diskDao;
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
    @Inject
    private VmJobsMonitoring vmJobsMonitoring;

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
                StoragePool storage_pool = storagePoolDao.get(vds.getStoragePoolId());
                if (StoragePoolStatus.Uninitialized != storage_pool
                        .getStatus()) {
                    vdsBroker.runVdsCommand(
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

    /**
     * The following method will clear a cache for problematic domains, which were reported by vds
     */
    private void clearDomainCache(final VDS vds) {
        eventQueue.submitEventSync(new Event(vds.getStoragePoolId(),
                null, vds.getId(), EventType.VDSCLEARCACHE, ""),
                () -> {
                    IrsBrokerCommand.clearVdsFromCache(vds.getStoragePoolId(), vds.getId(), vds.getName());
                    return new EventResult(true, EventType.VDSCLEARCACHE);
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
        if (vmIds.isEmpty()) {
            return;
        }

        vmJobsMonitoring.removeJobsByVmIds(vmIds);
        ThreadPoolUtil.execute(() -> processOnVmStopInternal(vmIds, hostId));
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
     */
    @Override
    public void syncLunsInfoForBlockStorageDomain(final Guid storageDomainId, final Guid vdsId) {
        ThreadPoolUtil.execute(() -> {
            StorageDomainParametersBase parameters = new StorageDomainParametersBase(storageDomainId);
            parameters.setVdsId(vdsId);
            backend.runInternalAction(VdcActionType.SyncLunsInfoForBlockStorageDomain, parameters);
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
        ThreadPoolUtil.execute(() -> {
            log.info("ResourceManager::vdsNotResponding entered for Host '{}', '{}'",
                    vds.getId(),
                    vds.getHostName());

            FenceVdsActionParameters params = new FenceVdsActionParameters(vds.getId());
            backend.runInternalAction(VdcActionType.VdsNotRespondingTreatment,
                    params,
                    ExecutionHandler.createInternalJobContext());

            moveBricksToUnknown(vds);
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
            ThreadPoolUtil.execute(() -> {
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
        return vmsToMigrate.stream().map(
                vm -> {
                    MigrateVmToServerParameters parameters =
                            new MigrateVmToServerParameters(false,
                                    vm.getId(),
                                    vds.getId());
                    parameters.setShouldBeLogged(false);
                    return parameters;
                }).collect(Collectors.toList());
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
        IVdsAsyncCommand command = vdsBroker.getAsyncCommandForVm(vmId);

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

        haAutoStartVmsRunner.addVmsToRun(vmIds);
    }

    @Override
    public void runColdRebootVms(List<Guid> vmIds) {
        for (Guid vmId : vmIds) {
            AuditLogableBase event = new AuditLogableBase();
            event.setVmId(vmId);
            auditLogDirector.log(event, AuditLogType.COLD_REBOOT_VM_DOWN);

            log.info("VM is down as a part of cold reboot process. Attempting to restart. VM Name '{}', VM Id '{}",
                    event.getVmName(), vmId);
        }

        coldRebootAutoStartVmsRunner.addVmsToRun(vmIds);
    }

    @Override
    public void addUnmanagedVms(Guid hostId, List<Guid> unmanagedVmIds) {
        if (!unmanagedVmIds.isEmpty()) {
            backend.runInternalAction(
                    VdcActionType.AddUnmanagedVms,
                    new AddUnmanagedVmsParameters(hostId, unmanagedVmIds),
                    ExecutionHandler.createInternalJobContext());
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
        final IVdsAsyncCommand command = vdsBroker.getAsyncCommandForVm(vmId);
        if (command != null) {
            // The command will be invoked in a different VDS in its rerun method, so we're calling
            // its rerun method from a new thread so that it won't be executed within our current VDSM lock
            ThreadPoolUtil.execute(() -> command.rerun());
        }
    }

    @Override
    public void runningSucceded(Guid vmId) {
        IVdsAsyncCommand command = vdsBroker.getAsyncCommandForVm(vmId);
        if (command != null) {
            command.runningSucceded();
        }
    }

    @Override
    public void removeAsyncRunningCommand(Guid vmId) {
        IVdsAsyncCommand command = vdsBroker.removeAsyncRunningCommand(vmId);
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

        ThreadPoolUtil.execute(() -> {

            // Get Disks and CpuQos of VMs from the DB
            Map<Guid, List<Disk>> diskMap = diskDao.getAllForVms(vmIds);
            Map<Guid, CpuQos> cpuQosMap = cpuQosDao.getCpuQosByVmIds(vmIds);

            Map<Guid, List<DiskImage>> diskImageMap = new HashMap<>();
            Set<Guid> diskProfileIds = new HashSet<>();

            for (Guid vmId : vmIds) {
                // Filter - only plugged disk images with disk profile remain
                List<DiskImage> diskImages = diskMap.get(vmId).stream()
                        .filter(disk -> disk.getPlugged() && disk.getDiskStorageType() == DiskStorageType.IMAGE)
                        .map(DiskImage.class::cast)
                        .filter(disk -> disk.getDiskProfileId() != null)
                        .collect(Collectors.toList());

                diskImageMap.put(vmId, diskImages);

                for (DiskImage img : diskImages) {
                    diskProfileIds.add(img.getDiskProfileId());
                }
            }

            // Get StorageQos of used disk profiles
            Map<Guid, StorageQos> storageQosMap = storageQosDao.getQosByDiskProfileIds(diskProfileIds);

            // Call VmSlaPolicyCommand for each VM
            for (Guid vmId : vmIds) {
                CpuQos cpuQos = cpuQosMap.get(vmId);

                VmSlaPolicyParameters params = new VmSlaPolicyParameters(vmId, cpuQos);
                for (DiskImage diskImage : diskImageMap.get(vmId)) {
                    Guid diskProfileId = diskImage.getDiskProfileId();
                    StorageQos storageQos = storageQosMap.get(diskProfileId);

                    if(storageQos != null) {
                        params.getStorageQos().put(diskImage, storageQos);
                    }
                }

                if (!params.isEmpty()) {
                    backend.runInternalAction(VdcActionType.VmSlaPolicy, params);
                }
            }
        });
    }

    public void onError(@Observes final VDSNetworkException vdsException) {
        ThreadPoolUtil.execute(() -> resourceManagerProvider.get().getVdsManager(
                vdsException.getVdsError().getVdsId()).handleNetworkException(vdsException));
    }

    @Override
    public void refreshHostIfAnyVmHasHostDevices(final List<Guid> vmIds, final Guid hostId) {
        if (vmIds.isEmpty()) {
            return;
        }

        ThreadPoolUtil.execute(() -> {
            HostDeviceManager hostDeviceManager = Injector.get(HostDeviceManager.class);
            hostDeviceManager.refreshHostIfAnyVmHasHostDevices(vmIds, hostId);
        });
    }

    @Override
    public void refreshHostCapabilities(Guid hostId) {
        backend.runInternalAction(VdcActionType.RefreshHostCapabilities, new VdsActionParameters(hostId));
    }

    @Override
    public boolean isUpdateAvailable(VDS host) {
        return availableUpdatesFinder.isUpdateAvailable(host);
    }

    // TODO asynch event handler - design infra code to allow async events in segregated thread
    public void onMomPolicyChange(@Observes @MomPolicyUpdate final Cluster cluster) {
        if (cluster == null) {
            return;
        }
        List<VDS> activeHostsInCluster =
                vdsDao.getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);
        // collect all Active hosts into a callable list
        List<Callable<Object>> callables = new LinkedList<>();
        for (final VDS vds : activeHostsInCluster) {
            callables.add(() -> {
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
            });
        }
        // run all VDSCommands concurrently with executor
        if (callables.size() > 0) {
            ThreadPoolUtil.invokeAll(callables);
        }
    }

    @Override
    public void importHostedEngineVm(final VM vm) {
        ThreadPoolUtil.execute(() -> hostedEngineImporterProvider.get().doImport(vm));
    }
}
