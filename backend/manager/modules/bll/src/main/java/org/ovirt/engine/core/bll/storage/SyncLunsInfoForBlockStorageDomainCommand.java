package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Synchronize LUN details comprising the storage domain with the DB
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class SyncLunsInfoForBlockStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    public SyncLunsInfoForBlockStorageDomainCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeCommand() {
        final List<LUNs> lunsFromVgInfo = (List<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                new GetVGInfoVDSCommandParameters(getVds().getId(), getStorageDomain().getStorage())).getReturnValue();
        final List<LUNs> lunsFromDb = getLunDao().getAllForVolumeGroup(getStorageDomain().getStorage());

        if (isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb)) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    refreshLunsInfo(lunsFromVgInfo, lunsFromDb);
                    return null;
                }
            });
        }

        setSucceeded(true);
    }

    protected void refreshLunsInfo(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            // Update LUN
            LUNs lunFromDB = getLunDao().get(lunFromVgInfo.getLUN_id());
            if (lunFromDB == null) {
                getLunDao().save(lunFromVgInfo);
                log.infoFormat("New LUN discovered, ID: {0}", lunFromVgInfo.getLUN_id());
            }
            else if (lunFromDB.getDeviceSize() != lunFromVgInfo.getDeviceSize()) {
                getLunDao().update(lunFromVgInfo);
                log.infoFormat("Updated LUN device size - ID: {0}, previous size: {1}, new size: {2}.",
                        lunFromVgInfo.getLUN_id(), lunFromDB.getDeviceSize(), lunFromVgInfo.getDeviceSize());
            }

            // Update lun connections map
            for (StorageServerConnections connection : lunFromVgInfo.getLunConnections()) {
                StorageServerConnections connectionFromDb =
                        getStorageServerConnectionDAO().getForIqn(connection.getiqn());
                if (connectionFromDb == null) {
                    // Shouldn't happen
                    continue;
                }

                LUN_storage_server_connection_map lunConnection = new LUN_storage_server_connection_map(
                        lunFromVgInfo.getLUN_id(), connectionFromDb.getid());
                if (getStorageServerConnectionLunMapDao().get(lunConnection.getId()) == null) {
                    getStorageServerConnectionLunMapDao().save(lunConnection);
                }
            }
        }

        // Cleanup LUNs from DB
        for (LUNs lunFromDb : lunsFromDb) {
            if (!isDummyLun(lunFromDb) && !containsLun(lunsFromVgInfo, lunFromDb)) {
                getLunDao().remove(lunFromDb.getLUN_id());
                log.infoFormat("Removed LUN ID: {0}", lunFromDb.getLUN_id());
            }
        }
    }

    protected boolean isLunsInfoMismatch(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        if (lunsFromDb.size() != lunsFromVgInfo.size()) {
            return true;
        }

        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            for (LUNs lunFromDb : lunsFromDb) {
                if (lunFromDb.getphysical_volume_id() == null ||
                        !lunFromDb.getphysical_volume_id().equals(lunFromVgInfo.getphysical_volume_id())) {
                    continue;
                }

                if (!lunFromDb.getLUN_id().equals(lunFromVgInfo.getLUN_id())) {
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
        return lun.getLUN_id().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX);
    }

    private boolean containsLun(List<LUNs> luns, LUNs lunToFind) {
        for (LUNs lun : luns) {
            if (lun.getLUN_id().equals(lunToFind.getLUN_id())) {
                return true;
            }
        }
        return false;
    }

    protected StorageServerConnectionLunMapDAO getStorageServerConnectionLunMapDao() {
        return getDbFacade().getStorageServerConnectionLunMapDao();
    }
}
