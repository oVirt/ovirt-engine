package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ExtendVmDiskSizeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;

/**
 * Synchronizes all the direct LUNs that are attached to VMs in a given data center.
 * When given a set of direct LUN IDs with {@link SyncDirectLunsParameters#getDirectLunIds},
 * the command synchronizes only these direct LUNs.
 */
@NonTransactiveCommandAttribute
public class SyncDirectLunsCommand<T extends SyncDirectLunsParameters> extends AbstractSyncLunsCommand<T> {

    @Inject
    private DiskLunMapDao diskLunMapDao;

    @Inject
    private LunDao lunDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private DiskDao diskImageDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StorageServerConnectionDao serverConnectionDao;

    private Map<String, Guid> lunToDiskIdsOfDirectLunsAttachedToVmsInPool;
    private Map<Guid, Set<Guid>> lunsPerHost = new HashMap<>();

    public SyncDirectLunsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        // To sync all the direct luns that are attached to VMs in
        // the storage pool, a valid and active storage pool is required.
        if (getParameters().getDirectLunIds().isEmpty()) {
            return validate(createStoragePoolValidator().existsAndUp());
        }
        return canSyncDirectLun();
    }

    private boolean canSyncDirectLun() {
        if (!directLunExists()) {
            return false;
        }
        if (getParameters().getVdsId() != null) {
            return validateVds();
        }

        // Floating direct LUNs, and direct LUNs that attached to non-running VMs, don't require synchronization.
        Set<Guid> filterFloatingLuns = getParameters().getDirectLunIds()
                .stream()
                .filter(this::isDirectLunAttachedToRunningVm)
                .collect(Collectors.toSet());
        getParameters().setDirectLunIds(filterFloatingLuns);

        lunsPerHost = getLunsToScanOnHost();
        try {
            for (Map.Entry<Guid, Set<Guid>> hostInfo : lunsPerHost.entrySet()) {
                if (hostInfo.getValue().isEmpty()) {
                    return false;
                }
                getParameters().setVdsId(hostInfo.getKey());
                if (!validateVds()) {
                    return false;
                }
            }
        } finally {
            //Rollback to the initial state for each VDS
            getParameters().setVdsId(null);
        }
        return true;
    }

    private boolean directLunExists() {
        for (Guid id : getParameters().getDirectLunIds()) {
            if (diskLunMapDao.getDiskLunMapByDiskId(id) == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        if (!syncDirectLuns()) {
            setCommandStatus(CommandStatus.FAILED);
            setSucceeded(false);
            return;
        }

        setSucceeded(true);
        auditLog(this, AuditLogType.SYNC_DIRECT_LUNS_FINISHED);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getIdsOfDirectLunsToSync()
                .collect(Collectors.toMap(Guid::toString, diskId -> LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED)));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return getIdsOfDirectLunsToSync()
                .map(id -> new PermissionSubject(id, VdcObjectType.Disk, getActionType().getActionGroup()))
                .collect(Collectors.toList());
    }

    private boolean syncDirectLuns() {
        auditLog(this, AuditLogType.SYNC_DIRECT_LUNS_STARTED);
        Set<LUNs> lunsToUpdateInDb = getLunsToUpdateInDb();
        if (lunsToUpdateInDb.isEmpty()) {
            log.info("Could not find any attached LUNs to update.");
            return true;
        }
        lunDao.updateAllInBatch(lunsToUpdateInDb);
        log.info(lunsToUpdateInDb.stream().map(LUNs::getLUNId).collect(
                Collectors.joining(", ", "LUNs with IDs: [", "] were updated in the DB.")));
        return updateLunDisksOnGuests(lunsToUpdateInDb);
    }

    private boolean isDirectLunAttachedToRunningVm (Guid directLunId) {
        List<VM> pluggedVms = getPluggedVms(directLunId);
        return pluggedVms != null && pluggedVms.stream().map(VM::getRunOnVds).anyMatch(Objects::nonNull);
    }

    private boolean updateLunDisksOnGuests(Set<LUNs> lunsToUpdateInDb) {
        // This method updates the LUN disks on the guests while they are running.
        for (LUNs lun : lunsToUpdateInDb) {
            int lunSize = lun.getDeviceSize();
            List<VM> pluggedVms = getPluggedVms(lun.getDiskId());
            for (VM vm : pluggedVms) {
                if (vm.getStatus() != VMStatus.Up || vm.getRunOnVds() == null) {
                    continue;
                }
                VDSReturnValue returnValue;
                ExtendVmDiskSizeVDSCommandParameters params =
                        new ExtendVmDiskSizeVDSCommandParameters(vm.getRunOnVds(), vm.getId(), lun.getLUNId(), lunSize);
                try {
                    returnValue = runVdsCommand(VDSCommandType.ExtendVmDiskSize, params);
                } catch (EngineException e) {
                    log.error("Failed to update the size for LUN disk '{}' due to error, "
                                    + "VM should be restarted to detect the new size: {}",
                            lun.getDiskId(), e.getMessage());
                    auditLog(this, AuditLogType.SYNC_DIRECT_LUNS_FAILED);
                    return false;
                }

                if (returnValue.getSucceeded()) {
                    addCustomValue("VmId", vm.getId().toString());
                    addCustomValue("DiskId", lun.getDiskId().toString());
                    auditLog(this, AuditLogType.SYNC_DIRECT_LUNS_FINISHED_ON_GUEST);
                }
            }
        }
        return true;
    }

    private List<VM> getPluggedVms(Guid diskId) {
        return vmDao.getForDisk(diskId, false).get(Boolean.TRUE);
    }

    private Stream<Guid> getIdsOfDirectLunsToSync() {
        Set<Guid> lunIds = getParameters().getDirectLunIds();
        return lunIds.isEmpty() ? getLunToDiskIdsOfDirectLunsAttachedToVmsInPool().values().stream() : lunIds.stream();
    }

    protected Set<LUNs> getLunsToUpdateInDb() {
        Map<String, Guid> lunToDirectLunIds;
        if (getParameters().getDirectLunIds().isEmpty()) {
            // Fetch all the direct LUNs attached to VMs in the storage pool,
            // regardless of whether the user selected them or not.
            lunToDirectLunIds = getLunToDiskIdsOfDirectLunsAttachedToVmsInPool();
            if (lunToDirectLunIds != null && !lunToDirectLunIds.isEmpty()) {
                return getLuns(getParameters().getVdsId(), lunToDirectLunIds);
            }
            return Collections.emptySet();
        }

        if (getParameters().getVdsId() != null || lunsPerHost.isEmpty()) {
            lunToDirectLunIds = getParameters().getDirectLunIds().stream()
                    .map(diskLunMapDao::getDiskLunMapByDiskId)
                    .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId));
            return getLuns(getParameters().getVdsId(), lunToDirectLunIds);
        }

        Set<LUNs> lunsToUpdate = new HashSet<>();
        for (Map.Entry<Guid, Set<Guid>> hostInfo : lunsPerHost.entrySet()) {
            lunToDirectLunIds = hostInfo.getValue().stream()
                    .map(diskLunMapDao::getDiskLunMapByDiskId)
                    .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId));
            Set<LUNs> luns = getLuns(hostInfo.getKey(), lunToDirectLunIds);
            lunsToUpdate.addAll(luns);
        }
        return lunsToUpdate;
    }

    protected Map<String, Guid> getLunToDiskIdsOfDirectLunsAttachedToVmsInPool() {
        if (lunToDiskIdsOfDirectLunsAttachedToVmsInPool == null) {
            lunToDiskIdsOfDirectLunsAttachedToVmsInPool =
                    diskLunMapDao.getDiskLunMapsForVmsInPool(getParameters().getStoragePoolId())
                            .stream()
                            .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId));
        }
        return lunToDiskIdsOfDirectLunsAttachedToVmsInPool;
    }

    private Map<Guid, Set<Guid>> getLunsToScanOnHost() {
        Map<Guid, Set<Guid>> luns = new HashMap<>();

        getParameters().getDirectLunIds().forEach(lun -> {
            List<VM> pluggedVms = getPluggedVms(lun);
            if (pluggedVms != null && !pluggedVms.isEmpty()) {
                pluggedVms.stream().map(VM::getRunOnVds).filter(Objects::nonNull).forEach(hostId -> {
                    luns.putIfAbsent(hostId, new HashSet<>());
                    luns.get(hostId).add(lun);
                });
            }
        });
        return luns;
    }

    private Set<LUNs> getLuns(Guid hostId, Map<String, Guid> lunToDirectLunIds) {
        Set<String> lunsIds = lunToDirectLunIds.keySet();
        try {
            return getDeviceList(lunsIds, hostId).stream()
                    .peek(lun -> {
                        LUNs lunFromDB = lunDao.get(lun.getId());
                        if (lunFromDB.getVolumeGroupId().isEmpty()) {
                            lun.setPhysicalVolumeId(null);
                            lun.setVolumeGroupId("");
                        }

                        if (lunFromDB.getLunConnections() == null) {
                            lun.setLunConnections(serverConnectionDao.getAllForLun(lun.getId()));
                        }
                    })
                    .peek(lun -> lun.setDiskId(lunToDirectLunIds.get(lun.getLUNId())))
                    .collect(Collectors.toSet());
        } catch (RuntimeException e) {
            log.error("Failed to update LUNs, VDS connectivity error for LUNs IDs: {} on host: {}, error details {}",
                    lunsIds, hostId, e.getMessage());
            return new HashSet<>();
        }
    }
}
