package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
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

    @Override
    protected void executeCommand() {
        final List<LUNs> lunsFromVgInfo = (List<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                new GetVGInfoVDSCommandParameters(getVds().getId(), getStorageDomain().getStorage())).getReturnValue();
        final List<LUNs> lunsFromDb = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());

        if (isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb)) {
            TransactionSupport.executeInNewTransaction(() -> {
                refreshLunsInfo(lunsFromVgInfo, lunsFromDb);
                return null;
            });
        }

        setSucceeded(true);
    }

    protected void refreshLunsInfo(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            // Update LUN
            LUNs lunFromDB = getLunDao().get(lunFromVgInfo.getLUNId());
            if (lunFromDB == null) {
                getLunDao().save(lunFromVgInfo);
                log.info("New LUN discovered, ID '{}'", lunFromVgInfo.getLUNId());
            }
            else if (lunFromDB.getDeviceSize() != lunFromVgInfo.getDeviceSize()) {
                getLunDao().update(lunFromVgInfo);
                log.info("Updated LUN device size - ID '{}', previous size {}, new size {}.",
                        lunFromVgInfo.getLUNId(), lunFromDB.getDeviceSize(), lunFromVgInfo.getDeviceSize());
            }

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

        // Cleanup LUNs from DB
        for (LUNs lunFromDb : lunsFromDb) {
            if (!isDummyLun(lunFromDb) && !containsLun(lunsFromVgInfo, lunFromDb)) {
                getLunDao().remove(lunFromDb.getLUNId());
                log.info("Removed LUN ID '{}'", lunFromDb.getLUNId());
            }
        }
    }

    protected boolean isLunsInfoMismatch(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        if (lunsFromDb.size() != lunsFromVgInfo.size()) {
            return true;
        }

        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            for (LUNs lunFromDb : lunsFromDb) {
                if (lunFromDb.getPhysicalVolumeId() == null ||
                        !lunFromDb.getPhysicalVolumeId().equals(lunFromVgInfo.getPhysicalVolumeId())) {
                    continue;
                }

                if (!lunFromDb.getLUNId().equals(lunFromVgInfo.getLUNId())) {
                    return true;
                }
                else if (lunFromDb.getDeviceSize() != lunFromVgInfo.getDeviceSize()) {
                    // Size mismatch detected - refresh info is needed
                    return true;
                }
            }
        }

        return false;
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
