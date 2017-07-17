package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

@Singleton
public class LunHelper {

    @Inject
    private LunDao lunDao;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @Inject
    private ISCSIStorageHelper iscsiStorageHelper;

    public void proceedDirectLUNInDb(final LUNs lun, StorageType storageType) {
        lun.setPhysicalVolumeId(null);
        proceedLUNInDb(lun, storageType, "");
    }

    public void proceedLUNInDb(final LUNs lun, StorageType storageType, String volumeGroupId) {
        lun.setVolumeGroupId(volumeGroupId);
        if (lunDao.get(lun.getLUNId()) == null) {
            lunDao.save(lun);
        } else if (!volumeGroupId.isEmpty()) {
            lunDao.update(lun);
        }

        if (storageType == StorageType.FCP) {
            // No need to handle connections (FCP storage doesn't utilize connections).
            return;
        }

        for (StorageServerConnections connection : lun.getLunConnections()) {
            StorageServerConnections dbConnection = iscsiStorageHelper.findConnectionWithSameDetails(connection);
            if (dbConnection == null) {
                connection.setId(Guid.newGuid().toString());
                connection.setStorageType(storageType);
                storageServerConnectionDao.save(connection);
            } else {
                connection.setId(dbConnection.getId());
            }
            if (storageServerConnectionLunMapDao.get
                    (new LUNStorageServerConnectionMapId(lun.getLUNId(), connection.getId())) == null) {
                storageServerConnectionLunMapDao.save(
                        new LUNStorageServerConnectionMap(lun.getLUNId(), connection.getId()));
            }
        }
    }
}
