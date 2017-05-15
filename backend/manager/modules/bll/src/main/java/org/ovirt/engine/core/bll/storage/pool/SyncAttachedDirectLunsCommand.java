package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SyncAttachedDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;

/**
 * Synchronizes all the direct luns that are attached to VMs in a given data center.
 * If the list of direct luns IDs from the command's parameters,
 * {@link SyncAttachedDirectLunsParameters::getAttachedDirectLunDisksIds}, is not null,
 * the command synchronizes only the direct luns noted by the IDs from that list.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncAttachedDirectLunsCommand<T extends SyncAttachedDirectLunsParameters>
        extends AbstractSyncLunsCommand<T> {

    @Inject
    private DiskLunMapDao diskLunMapDao;

    @Inject
    private LunDao lunDao;

    private Map<Guid, String> diskToLunIdsOfDirectLunsAttachedToVmsInPool;

    public SyncAttachedDirectLunsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (!validateAttachedDirectLuns()) {
            return false;
        }

        return true;
    }

    /**
     * Verifies that the list of attached direct luns from the command's parameters
     * indeed contains only direct luns that are attached to a vm in the storage pool.
     */
    protected boolean validateAttachedDirectLuns() {
        if (getParameters().getAttachedDirectLunDisksIds() == null) {
            log.debug("Synchronizing all the direct LUNs that are attached to VMs in data center '{}', id '{}'.",
                    getStoragePoolName(), getStoragePoolId());
            return true;
        }
        Map<Guid, String> diskToLunIds = getDiskToLunIdsOfDirectLunsAttachedToVmsInPool();
        Collection<String> directLunsNotAttachedToVmsInPool = getParameters().getAttachedDirectLunDisksIds()
                .stream()
                .filter(diskId -> !diskToLunIds.containsKey(diskId))
                .map(Guid::toString)
                .collect(Collectors.toList());
        return directLunsNotAttachedToVmsInPool.isEmpty() ||
                failValidation(
                        EngineMessage.ACTION_TYPE_FAILED_CANNOT_SYNC_DIRECT_LUN_DISKS_NOT_ATTACHED_TO_VM_IN_POOL,
                        String.format("$diskIds %s",
                                directLunsNotAttachedToVmsInPool.stream().sorted().collect(Collectors.joining(", "))),
                        String.format("$storagePoolId %s", getParameters().getStoragePoolId()));
    }

    @Override
    protected void executeCommand() {
        Collection<LUNs> lunsToUpdateInDb = getLunsToUpdateInDb();
        lunDao.updateAllInBatch(lunsToUpdateInDb);
        log.info(lunsToUpdateInDb.stream().map(LUNs::getLUNId).collect(
                Collectors.joining(", ", "LUNs with IDs: [", "] were updated in the DB.")));
        setSucceeded(true);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Set<Guid> directLunsToLock = Optional.ofNullable(getParameters().getAttachedDirectLunDisksIds())
                .orElse(getDiskToLunIdsOfDirectLunsAttachedToVmsInPool().keySet());
        return directLunsToLock.stream()
                .collect(Collectors.toMap(Guid::toString, diskId -> LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED)));
    }

    protected Collection<LUNs> getLunsToUpdateInDb() {
        Map<Guid, String> diskToLunIds = getDiskToLunIdsOfDirectLunsAttachedToVmsInPool();
        Map<String, Guid> lunToDiskIds = diskToLunIds.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        List<String> lunsIds = Optional.ofNullable(getParameters().getAttachedDirectLunDisksIds())
                .map(set -> set.stream().map(diskToLunIds::get).collect(Collectors.toList()))
                .orElse(null);
        return getDeviceList(lunsIds)
                .stream()
                .filter(lun -> lunToDiskIds.containsKey(lun.getLUNId()))
                .peek(lun -> lun.setVolumeGroupId(""))
                .peek(lun -> lun.setDiskId(lunToDiskIds.get(lun.getLUNId())))
                .collect(Collectors.toList());
    }

    protected Map<Guid, String> getDiskToLunIdsOfDirectLunsAttachedToVmsInPool() {
        if (diskToLunIdsOfDirectLunsAttachedToVmsInPool == null) {
            diskToLunIdsOfDirectLunsAttachedToVmsInPool =
                    diskLunMapDao.getDiskLunMapsForVmsInPool(getParameters().getStoragePoolId())
                            .stream()
                            .collect(Collectors.toMap(DiskLunMap::getDiskId, DiskLunMap::getLunId));
        }
        return diskToLunIdsOfDirectLunsAttachedToVmsInPool;
    }
}
