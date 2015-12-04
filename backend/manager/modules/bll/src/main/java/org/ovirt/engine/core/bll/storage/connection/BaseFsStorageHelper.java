package org.ovirt.engine.core.bll.storage.connection;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseFsStorageHelper extends StorageHelperBase {
    private static final Logger log = LoggerFactory.getLogger(BaseFsStorageHelper.class);

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        Pair<Boolean, EngineFault> result;
        StorageServerConnections connection = DbFacade.getInstance().getStorageServerConnectionDao().get(
                storageDomain.getStorage());
        if (connection != null) {
            VdcReturnValueBase returnValue = Backend
                    .getInstance()
                    .runInternalAction(VdcActionType.forValue(type),
                            new StorageServerConnectionParametersBase(connection, vdsId));
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
        StorageServerConnections connection =
                DbFacade.getInstance().getStorageServerConnectionDao().get(storageDomain.getStorage());

        if (connection != null) {
            DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getId());
        }

        return true;
    }

    protected abstract StorageType getType();
}
