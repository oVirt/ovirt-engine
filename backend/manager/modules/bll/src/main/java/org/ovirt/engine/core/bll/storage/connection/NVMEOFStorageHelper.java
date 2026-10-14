package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@Singleton
public class NVMEOFStorageHelper extends BlockStorageHelperBase {

    @Override
    public Collection<StorageType> getTypes() {
        return Collections.singleton(StorageType.NVMEOF);
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.NVMEOF;
    }

    public StorageServerConnections findConnectionWithSameDetails(StorageServerConnections connection) {
        List<StorageServerConnections> connections = storageServerConnectionDao.getAllForStorage(connection.getConnection());
        for (StorageServerConnections dbConnection : connections) {
            if (dbConnection.getStorageType() == StorageType.NVMEOF
                    && Objects.equals(dbConnection.getNqn(), connection.getNqn())
                    && Objects.equals(dbConnection.getTrsvcid(), connection.getTrsvcid())
                    && Objects.equals(dbConnection.getHostNqn(), connection.getHostNqn())) {
                return dbConnection;
            }
        }
        return null;
    }
}
