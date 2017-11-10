package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * Synchronizes all the direct luns that are attached to VMs in a given data center.
 * When given a direct lun ID from {@link SyncDirectLunsParameters#getDirectLunId},
 * the command synchronizes only that direct lun.
 */
@NonTransactiveCommandAttribute
public class SyncDirectLunsCommand<T extends SyncDirectLunsParameters> extends AbstractSyncLunsCommand<T> {

    @Inject
    private DiskLunMapDao diskLunMapDao;

    @Inject
    private LunDao lunDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    private Map<String, Guid> lunToDiskIdsOfDirectLunsAttachedToVmsInPool;

    public SyncDirectLunsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (getParameters().getDirectLunId() == null) {
            // To sync all the direct luns that are attached to VMs in
            // the storage pool, a valid and active storage pool is required.
            return validate(createStoragePoolValidator().existsAndUp());
        }
        return canSyncDirectLun();
    }

    private boolean canSyncDirectLun() {
        return validateVds() &&
                directLunExists();
    }

    private boolean directLunExists() {
        return diskLunMapDao.getDiskLunMapByDiskId(getParameters().getDirectLunId()) != null ||
                failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Override
    protected void executeCommand() {
        Collection<LUNs> lunsToUpdateInDb = getLunsToUpdateInDb();
        if (lunsToUpdateInDb.size() > 0) {
            lunDao.updateAllInBatch(lunsToUpdateInDb);
            log.info(lunsToUpdateInDb.stream().map(LUNs::getLUNId).collect(
                    Collectors.joining(", ", "LUNs with IDs: [", "] were updated in the DB.")));
        } else {
            log.info("Could not find any LUNs to update.");
        }

        setSucceeded(true);
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
        return Optional.ofNullable(getParameters().getDirectLunId())
                .map(Stream::of)
                .orElse(getLunToDiskIdsOfDirectLunsAttachedToVmsInPool().values().stream());
    }

    protected Collection<LUNs> getLunsToUpdateInDb() {
        Map<String, Guid> lunToDirectLunIds = Optional.ofNullable(getParameters().getDirectLunId())
                .map(diskLunMapDao::getDiskLunMapByDiskId)
                .map(diskLunMap -> Stream.of(diskLunMap)
                        .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId)))
                .orElse(getLunToDiskIdsOfDirectLunsAttachedToVmsInPool());

        Set<String> lunsIds = lunToDirectLunIds.keySet();
        return getDeviceList(lunsIds)
                .stream()
                .peek(lun -> {
                    if (lunDao.get(lun.getId()).getVolumeGroupId().isEmpty()) {
                        lun.setPhysicalVolumeId(null);
                        lun.setVolumeGroupId("");
                    }
                })
                .peek(lun -> lun.setDiskId(lunToDirectLunIds.get(lun.getLUNId())))
                .collect(Collectors.toList());
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
}
