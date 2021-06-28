package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SaveVmExternalDataParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.SyncStorageDomainsLunsParameters;
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
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.qualifiers.MomPolicyUpdate;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxy;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProxyManager;
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
    private LockManager lockManager;
    @Inject
    private HaAutoStartVmsRunner haAutoStartVmsRunner;
    @Inject
    private ColdRebootAutoStartVmsRunner coldRebootAutoStartVmsRunner;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private CpuQosDao cpuQosDao;
    @Inject
    private StorageQosDao storageQosDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
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
    @Inject
    private ExecutionHandler executionHandler;
    @Inject
    private IrsProxyManager irsProxyManager;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private StoragePoolStatusHandler storagePoolStatusHandler;
    @Inject
    private HostLocking hostLocking;

    private static final Logger log = LoggerFactory.getLogger(VdsEventListener.class);

    @Override
    public void vdsMovedToMaintenance(VDS vds) {
        try {
            processStorageOnVdsInactive(vds);
        } finally {
            executionHandler.updateSpecificActionJobCompleted(vds.getId(), ActionType.MaintenanceVds, true);
        }
    }

    private void processStorageOnVdsInactive(final VDS vds) {

        // Clear the problematic timers since the VDS is in maintenance so it doesn't make sense to check it
        // anymore.
        if (!Guid.Empty.equals(vds.getStoragePoolId())) {
            // when vds is being moved to maintenance, this is the part in which we disconnect it from the pool
            // and the storage server. it should be synced with the host autorecovery mechanism to try to avoid
            // leaving the host with storage/pool connection when it's on maintenance.
            EngineLock lock = new EngineLock(getVdsPoolAndStorageConnectionsLock(vds.getId()), null);
            try {
                lockManager.acquireLockWait(lock);
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
                    backend.runInternalAction(ActionType.DisconnectHostFromStoragePoolServers, params);
                }
            } finally {
                lockManager.releaseLock(lock);
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
                    clearVdsFromCache(vds.getStoragePoolId(), vds.getId(), vds.getName());
                    return new EventResult(true, EventType.VDSCLEARCACHE);
                });
    }

    /**
     * Remove a VDS entry from the pool's IRS Proxy cache, clearing the problematic domains for this VDS and their
     * timers if they need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since
     * we need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
     * active anymore, it won't be connected to any of the domains).
     *
     * @param storagePoolId
     *            The ID of the storage pool to clean the IRS Proxy's cache for.
     * @param vdsId
     *            The ID of the VDS to remove from the cache.
     * @param vdsName
     *            The name of the VDS (for logging).
     */
    private void clearVdsFromCache(Guid storagePoolId, Guid vdsId, String vdsName) {
        IrsProxy irsProxyData = irsProxyManager.getProxy(storagePoolId);
        if (irsProxyData != null) {
            irsProxyData.clearVdsFromCache(vdsId, vdsName);
        }
    }

    @Override
    public EventResult storageDomainNotOperational(Guid storageDomainId, Guid storagePoolId) {
        StorageDomainPoolParametersBase parameters =
                new StorageDomainPoolParametersBase(storageDomainId, storagePoolId);
        parameters.setIsInternal(true);
        parameters.setInactive(true);
        boolean isSucceeded = backend.runInternalAction(ActionType.DeactivateStorageDomain,
                parameters,
                ExecutionHandler.createInternalJobContext()).getSucceeded();
        return new EventResult(isSucceeded, EventType.DOMAINNOTOPERATIONAL);
    }

    @Override
    public EventResult masterDomainNotOperational(Guid storageDomainId,
            Guid storagePoolId,
            boolean isReconstructToInactiveDomains,
            boolean canReconstructToCurrentMaster) {
        ActionParametersBase parameters =
                new ReconstructMasterParameters(storagePoolId,
                        storageDomainId,
                        true,
                        isReconstructToInactiveDomains,
                        canReconstructToCurrentMaster);
        boolean isSucceeded = backend.runInternalAction(ActionType.ReconstructMasterDomain,
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
            backend.runInternalAction(ActionType.ProcessDownVm,
                    new ProcessDownVmParameters(vmId, true));
        }

        HostDeviceManager hostDeviceManager = Injector.get(HostDeviceManager.class);
        hostDeviceManager.refreshHostIfAnyVmHasHostDevices(vmIds, hostId);
    }

    /**
     * Synchronizes all the given storage domains' LUNs details with the DB
     */
    @Override
    public void syncStorageDomainsLuns(Guid vdsId, Collection<Guid> storageDomainsToSync) {
        ThreadPoolUtil.execute(() -> {
            backend.runInternalAction(ActionType.SyncStorageDomainsLuns, new SyncStorageDomainsLunsParameters(
                    vdsId, storageDomainsToSync));
        });
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId) {
        StorageDomainStatic storageDomain = storageDomainStaticDao.get(domainId);
        Map<String, String> customLogValues = null;
        if (storageDomain != null) {
            customLogValues = Collections.singletonMap("StorageDomainNames", storageDomain.getName());
        }
        vdsNonOperational(vdsId, reason, logCommand, domainId, customLogValues);
    }

    @Override
    public void vdsNonOperational(Guid vdsId, NonOperationalReason reason, boolean logCommand, Guid domainId,
            Map<String, String> customLogValues) {
        executionHandler.updateSpecificActionJobCompleted(vdsId, ActionType.MaintenanceVds, false);
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(vdsId, reason, customLogValues);
        tempVar.setStorageDomainId(domainId);
        tempVar.setShouldBeLogged(logCommand);
        backend.runInternalAction(ActionType.SetNonOperationalVds,
                tempVar,
                ExecutionHandler.createInternalJobContext());
    }

    @Override
    public void vdsNotResponding(final VDS vds) {
        executionHandler.updateSpecificActionJobCompleted(vds.getId(), ActionType.MaintenanceVds, false);
        ThreadPoolUtil.execute(() -> {
            log.info("ResourceManager::vdsNotResponding entered for Host '{}', '{}'",
                    vds.getId(),
                    vds.getHostName());

            FenceVdsActionParameters params = new FenceVdsActionParameters(vds.getId());
            backend.runInternalAction(ActionType.VdsNotRespondingTreatment,
                    params,
                    ExecutionHandler.createInternalJobContext());

            moveBricksToUnknown(vds);
        });
    }

    private void moveBricksToUnknown(final VDS vds) {
        List<GlusterBrickEntity> brickEntities = glusterBrickDao.getGlusterVolumeBricksByServerId(vds.getId());
        for (GlusterBrickEntity brick : brickEntities) {
            if (brick.getStatus() == GlusterStatus.UP) {
                brick.setStatus(GlusterStatus.UNKNOWN);
            }
        }
        glusterBrickDao.updateBrickStatuses(brickEntities);
    }

    @Override
    public boolean vdsUpEvent(final VDS vds) {
        HostStoragePoolParametersBase params = new HostStoragePoolParametersBase(vds);
        CommandContext commandContext = new CommandContext(new EngineContext()).withoutExecutionContext();
        commandContext.getExecutionContext().setJobRequired(true);
        return backend.runInternalAction(ActionType.InitVdsOnUp, params, commandContext).getSucceeded();
    }

    @Override
    public boolean connectHostToDomainsInActiveOrUnknownStatus(VDS vds) {
        StoragePool sp = storagePoolDao.get(vds.getStoragePoolId());
        ConnectHostToStoragePoolServersParameters params =
                new ConnectHostToStoragePoolServersParameters(sp, vds, false);
        return backend
                .runInternalAction(ActionType.ConnectHostToStoragePoolServers, params)
                .getSucceeded();
    }

    @Override
    public void processOnCpuFlagsChange(Guid vdsId) {
        backend.runInternalAction(ActionType.HandleVdsCpuFlagsOrClusterChanged,
                new VdsActionParameters(vdsId));
    }

    @Override
    public void handleVdsVersion(Guid vdsId) {
        backend.runInternalAction(ActionType.HandleVdsVersion, new VdsActionParameters(vdsId));
    }

    @Override
    public void handleVdsFips(Guid vdsId) {
        backend.runInternalAction(ActionType.HandleVdsFips, new VdsActionParameters(vdsId));
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
        commandCoordinatorUtil.addStoragePoolExistingTasks(storagePool);
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
        backend.runInternalAction(ActionType.SetStoragePoolStatus, tempVar);
    }

    @Override
    public void storagePoolStatusChanged(Guid storagePoolId, StoragePoolStatus status) {
        storagePoolStatusHandler.poolStatusChanged(storagePoolId, status);
    }

    @Override
    public void runFailedAutoStartVMs(List<Guid> vmIds) {
        for (Guid vmId : vmIds) {
            // Alert that the virtual machine failed:
            AuditLogable event = createVmEvent(vmId, AuditLogType.HA_VM_FAILED);
            log.info("Highly Available VM went down. Attempting to restart. VM Name '{}', VM Id '{}'",
                    event.getVmName(), vmId);
        }

        haAutoStartVmsRunner.addVmsToRun(vmIds);
    }

    @Override
    public void runColdRebootVms(List<Guid> vmIds) {
        for (Guid vmId : vmIds) {
            AuditLogable event = createVmEvent(vmId, AuditLogType.COLD_REBOOT_VM_DOWN);
            log.info("VM is down as a part of cold reboot process. Attempting to restart. VM Name '{}', VM Id '{}",
                    event.getVmName(), vmId);
        }

        coldRebootAutoStartVmsRunner.addVmsToRun(vmIds);
    }

    private AuditLogable createVmEvent(Guid vmId, AuditLogType logType) {
        AuditLogable event = new AuditLogableImpl();
        event.setVmName(vmStaticDao.get(vmId).getName());
        event.setVmId(vmId);
        auditLogDirector.log(event, logType);
        return event;
    }

    @Override
    public void addUnmanagedVms(Guid hostId, List<Guid> unmanagedVmIds) {
        if (!unmanagedVmIds.isEmpty()) {
            backend.runInternalAction(
                    ActionType.AddUnmanagedVms,
                    new AddUnmanagedVmsParameters(hostId, unmanagedVmIds),
                    ExecutionHandler.createInternalJobContext());
        }
    }

    @Override
    public void handleVdsMaintenanceTimeout(Guid vdsId) {
        // try to put the host to Maintenance again.
        backend.runInternalAction(ActionType.MaintenanceNumberOfVdss,
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
    public void migrationProgressReported(Guid vmId, int progress) {
        IVdsAsyncCommand command = vdsBroker.getAsyncCommandForVm(vmId);
        if (command != null) {
            command.migrationProgressReported(progress);
        }
    }

    @Override
    public void actualDowntimeReported(Guid vmId, int actualDowntime) {
        IVdsAsyncCommand command = vdsBroker.getAsyncCommandForVm(vmId);
        if (command != null) {
            command.actualDowntimeReported(actualDowntime);
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
                    backend.runInternalAction(ActionType.VmSlaPolicy, params);
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
        ActionParametersBase parameters = new VdsActionParameters(hostId);
        parameters.setLockProperties(
                LockProperties.create(LockProperties.Scope.Execution).withWaitForever());
        backend.runInternalAction(ActionType.RefreshHostCapabilities, parameters);
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
    public void restartVmsWithLease(List<Guid> vmIds, Guid hostId) {
        if (vmIds.isEmpty()) {
            return;
        }

        EngineLock engineLock = new EngineLock(Collections.emptyMap(), Collections.emptyMap());
        ThreadPoolUtil.execute(() -> {
            log.info("trying to run VMs with a lease on a non-responding host {} elsewhere", hostId);
            for (Guid vmId : vmIds) {
                resourceManagerProvider.get().removeAsyncRunningVm(vmId);
                backend.runInternalAction(
                        ActionType.RunVm,
                        buildRunVmParameters(vmId),
                        ExecutionHandler.createInternalJobContext(engineLock));
            }
        });
    }

    public Map<String, Pair<String, String>> getVdsPoolAndStorageConnectionsLock(Guid vdsId) {
        return hostLocking.getVdsPoolAndStorageConnectionsLock(vdsId);
    }

    private RunVmParams buildRunVmParameters(Guid vmId) {
        RunVmParams parameters = new RunVmParams(vmId);
        parameters.setRunInUnknownStatus(true);
        return parameters;
    }

    public ActionReturnValue saveExternalData(SaveVmExternalDataParameters parameters) {
        return backend.runInternalAction(ActionType.SaveVmExternalData, parameters);
    }
}
