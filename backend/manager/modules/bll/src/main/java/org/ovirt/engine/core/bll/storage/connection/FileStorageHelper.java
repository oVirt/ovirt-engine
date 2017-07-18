package org.ovirt.engine.core.bll.storage.connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileStorageHelper extends StorageHelperBase {
    private static final Logger log = LoggerFactory.getLogger(FileStorageHelper.class);

    @Override
    public Collection<StorageType> getTypes() {
        return Arrays.asList(StorageType.LOCALFS, StorageType.NFS, StorageType.POSIXFS);
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        Pair<Boolean, EngineFault> result;
        StorageServerConnections connection = storageServerConnectionDao.get(storageDomain.getStorage());
        if (connection != null) {
            ActionReturnValue returnValue = backend
                    .runInternalAction(ActionType.forValue(type),
                            new StorageServerConnectionParametersBase(connection, vdsId, false));
            result = new Pair<>(returnValue.getSucceeded(), returnValue.getFault());
        } else {
            result = new Pair<>(false, null);
            log.warn("Did not connect host '{}' to storage domain '{}' because connection for connectionId '{}' is null.",
                    vdsId,
                    storageDomain.getStorageName(),
                    storageDomain.getStorage());
        }
        return result;
    }

    @Override
    public boolean isConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        boolean result = true;
        for (Map.Entry<String, String> entry : returnValue.entrySet()) {
            if (!"0".equals(entry.getValue())) {
                String connectionField = addToAuditLogErrorMessage(entry.getKey(), entry.getValue(), connections);
                printLog(log, connectionField, entry.getValue());
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        StorageServerConnections connection = storageServerConnectionDao.get(storageDomain.getStorage());

        if (connection != null) {
            storageServerConnectionDao.remove(connection.getId());
        }

        return true;
    }
}
