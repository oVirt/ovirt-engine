package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
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
@InternalCommandAttribute
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
            return canSyncAllDirectLunsAttachedToVmsInPool();
        }
        return canSyncDirectLun();
    }

    private boolean canSyncAllDirectLunsAttachedToVmsInPool() {
        // To sync all the direct luns that are attached to VMs in
        // the storage pool, a valid storage pool ID is required.
        return (getParameters().getStoragePoolId() != null && getStoragePool() != null)
                || failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    private boolean canSyncDirectLun() {
        HostValidator hostValidator = getHostValidator();
        return validate(hostValidator.hostExists()) &&
                validate(hostValidator.isUp()) &&
                directLunExists();
    }

    private boolean directLunExists() {
        return diskLunMapDao.getDiskLunMapByDiskId(getParameters().getDirectLunId()) != null ||
                failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    protected HostValidator getHostValidator() {
        return HostValidator.createInstance(getVds());
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
        Set<Guid> directLunsToLock = Optional.ofNullable(getParameters().getDirectLunId())
                .map(Collections::singleton)
                .orElse(new HashSet<>(getLunToDiskIdsOfDirectLunsAttachedToVmsInPool().values()));
        return directLunsToLock.stream()
                .collect(Collectors.toMap(Guid::toString, diskId -> LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED)));
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__SYNC);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    protected Collection<LUNs> getLunsToUpdateInDb() {
        Map<String, Guid> lunToDirectLunIds = Optional.ofNullable(getParameters().getDirectLunId())
                .map(diskLunMapDao::getDiskLunMapByDiskId)
                .map(diskLunMap -> Stream.of(diskLunMap)
                        .collect(Collectors.toMap(DiskLunMap::getLunId, DiskLunMap::getDiskId)))
                .orElse(getLunToDiskIdsOfDirectLunsAttachedToVmsInPool());

        List<String> lunsIds = new ArrayList<>(lunToDirectLunIds.keySet());
        return getDeviceList(lunsIds)
                .stream()
                .peek(lun -> lun.setVolumeGroupId(""))
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
