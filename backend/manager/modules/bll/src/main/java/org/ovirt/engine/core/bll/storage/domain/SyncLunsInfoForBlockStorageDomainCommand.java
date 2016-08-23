package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Synchronize LUN details comprising the storage domain with the DB
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class SyncLunsInfoForBlockStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    public SyncLunsInfoForBlockStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    public SyncLunsInfoForBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        final List<LUNs> lunsFromVgInfo = (List<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                new GetVGInfoVDSCommandParameters(getVds().getId(), getStorageDomain().getStorage())).getReturnValue();
        final List<LUNs> lunsFromDb = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());

        List<LUNs> lunsToUpdateInDb = getLunsToUpdateInDb(lunsFromVgInfo, lunsFromDb);
        if (lunsFromDb.size() != lunsFromVgInfo.size() || !lunsToUpdateInDb.isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                updateLunsInDb(lunsToUpdateInDb);
                refreshLunsConnections(lunsFromVgInfo);
                cleanupLunsFromDb(lunsFromVgInfo, lunsFromDb);
                return null;
            });
        }

        setSucceeded(true);
    }

    protected void refreshLunsConnections(List<LUNs> lunsFromVgInfo) {
        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            // Update lun connections map
            for (StorageServerConnections connection : lunFromVgInfo.getLunConnections()) {
                StorageServerConnections connectionFromDb =
                        getStorageServerConnectionDao().getForIqn(connection.getIqn());
                if (connectionFromDb == null) {
                    // Shouldn't happen
                    continue;
                }

                LUNStorageServerConnectionMap lunConnection = new LUNStorageServerConnectionMap(
                        lunFromVgInfo.getLUNId(), connectionFromDb.getId());
                if (getStorageServerConnectionLunMapDao().get(lunConnection.getId()) == null) {
                    getStorageServerConnectionLunMapDao().save(lunConnection);
                }
            }
        }
    }

    private void cleanupLunsFromDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        for (LUNs lunFromDb : lunsFromDb) {
            if (!isDummyLun(lunFromDb) && !containsLun(lunsFromVgInfo, lunFromDb)) {
                getLunDao().remove(lunFromDb.getLUNId());
                log.info("Removed LUN ID '{}'", lunFromDb.getLUNId());
            }
        }
    }

    protected List<LUNs> getLunsToUpdateInDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        List<LUNs> lunsToUpdateInDb = new LinkedList<>();
        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            for (LUNs lunFromDb : lunsFromDb) {
                if (lunFromDb.getPhysicalVolumeId() == null ||
                        !lunFromDb.getPhysicalVolumeId().equals(lunFromVgInfo.getPhysicalVolumeId())) {
                    continue;
                }

                if (!lunFromDb.getLUNId().equals(lunFromVgInfo.getLUNId()) ||
                        lunFromDb.getDeviceSize() != lunFromVgInfo.getDeviceSize()) {
                    lunsToUpdateInDb.add(lunFromVgInfo);
                }
            }
        }

        return lunsToUpdateInDb;
    }

    /**
     * Saves the new or updates the existing luns in the DB.
     */
    protected void updateLunsInDb(List<LUNs> lunsToUpdateInDb) {
        for (LUNs lunToUpdateInDb : lunsToUpdateInDb) {
            LUNs lunFromDB = getLunDao().get(lunToUpdateInDb.getLUNId());
            if (lunFromDB == null) {
                getLunDao().save(lunToUpdateInDb);
                log.info("New LUN discovered, ID '{}'", lunToUpdateInDb.getLUNId());
            } else {
                if (lunFromDB.getDeviceSize() != lunToUpdateInDb.getDeviceSize()) {
                    log.info("Updated LUN device size - ID '{}', previous size {}, new size {}.",
                            lunToUpdateInDb.getLUNId(), lunFromDB.getDeviceSize(), lunToUpdateInDb.getDeviceSize());
                } else {
                    log.info("Updated LUN information, ID '{}'.", lunToUpdateInDb.getLUNId());
                }
                getLunDao().update(lunToUpdateInDb);
            }
        }
    }

    private boolean isDummyLun(LUNs lun) {
        return lun.getLUNId().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX);
    }

    private boolean containsLun(List<LUNs> luns, LUNs lunToFind) {
        for (LUNs lun : luns) {
            if (lun.getLUNId().equals(lunToFind.getLUNId())) {
                return true;
            }
        }
        return false;
    }

    protected StorageServerConnectionLunMapDao getStorageServerConnectionLunMapDao() {
        return getDbFacade().getStorageServerConnectionLunMapDao();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.SYNC_LUNS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
