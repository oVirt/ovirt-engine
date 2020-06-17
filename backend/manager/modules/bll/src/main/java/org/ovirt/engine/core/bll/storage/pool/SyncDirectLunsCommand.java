package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
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
        auditLog(this, AuditLogType.SYNC_DIRECT_LUNS_STARTED);
        Collection<LUNs> lunsToUpdateInDb = getLunsToUpdateInDb();
        if (lunsToUpdateInDb.size() > 0) {
            lunDao.updateAllInBatch(lunsToUpdateInDb);
            log.info(lunsToUpdateInDb.stream().map(LUNs::getLUNId).collect(
                    Collectors.joining(", ", "LUNs with IDs: [", "] were updated in the DB.")));
        } else {
            log.info("Could not find any LUNs to update.");
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

    private Stream<Guid> getIdsOfDirectLunsToSync() {
        Set<Guid> lunIds = getParameters().getDirectLunIds();
        return lunIds.isEmpty() ? getLunToDiskIdsOfDirectLunsAttachedToVmsInPool().values().stream() : lunIds.stream();
    }

    protected Collection<LUNs> getLunsToUpdateInDb() {
        Map<String, Guid> lunToDirectLunIds;
        if (getParameters().getDirectLunIds().isEmpty()) {
            lunToDirectLunIds = getLunToDiskIdsOfDirectLunsAttachedToVmsInPool();
            return getLunList(getParameters().getVdsId(), lunToDirectLunIds);
        }

        if (getParameters().getVdsId() != null || lunsPerHost.isEmpty()) {
            lunToDirectLunIds = getParameters().getDirectLunIds().stream()
                    .map(diskLunMapDao::getDiskLunMapByDiskId)
                    .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId));
            return getLunList(getParameters().getVdsId(), lunToDirectLunIds);
        }

        Collection<LUNs> lunsToUpdate = new ArrayList<>();
        for (Map.Entry<Guid, Set<Guid>> hostInfo : lunsPerHost.entrySet()) {
            lunToDirectLunIds = hostInfo.getValue().stream()
                    .map(diskLunMapDao::getDiskLunMapByDiskId)
                    .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId));
            Collection<LUNs> luns = getLunList(hostInfo.getKey(), lunToDirectLunIds);
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
            List<VM> pluggedVms = vmDao.getForDisk(lun, false).get(Boolean.TRUE);
            if (pluggedVms != null && !pluggedVms.isEmpty()) {
                Guid hostId = pluggedVms.get(0).getRunOnVds();
                luns.putIfAbsent(hostId, new HashSet<>());
                luns.get(hostId).add(lun);
            }
        });
        return luns;
    }

    private Collection<LUNs> getLunList(Guid hostId, Map<String, Guid> lunToDirectLunIds) {
        Set<String> lunsIds = lunToDirectLunIds.keySet();
        try {
            return getDeviceList(lunsIds, hostId).stream()
                    .peek(lun -> {
                        if (lunDao.get(lun.getId()).getVolumeGroupId().isEmpty()) {
                            lun.setPhysicalVolumeId(null);
                            lun.setVolumeGroupId("");
                        }
                    })
                    .peek(lun -> lun.setDiskId(lunToDirectLunIds.get(lun.getLUNId())))
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error("Failed to update LUNs, VDS connectivity error for LUNs IDs: {} on host: {}, error details {}",
                    lunsIds, hostId, e.getMessage());
            return new HashSet<>();
        }
    }
}
