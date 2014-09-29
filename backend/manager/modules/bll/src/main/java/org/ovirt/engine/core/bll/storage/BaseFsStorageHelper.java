package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class BaseFsStorageHelper extends StorageHelperBase {

    @Override
    protected Pair<Boolean, VdcFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        Pair<Boolean, VdcFault> result;
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
            log.warn("Did not connect host: " + vdsId + " to storage domain: " + storageDomain.getStorageName()
                    + " because connection for connectionId:" + storageDomain.getStorage() + " is null.");
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
    public List<StorageServerConnections> getStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return new ArrayList<StorageServerConnections>(
                Arrays.asList(new StorageServerConnections[] { DbFacade.getInstance()
                        .getStorageServerConnectionDao().get(storageDomain.getStorage()) }));
    }

    @Override
    public boolean storageDomainRemoved(StorageDomainStatic storageDomain) {
        StorageServerConnections connection =
                DbFacade.getInstance().getStorageServerConnectionDao().get(storageDomain.getStorage());

        if (connection != null) {
            DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getid());
        }

        return true;
    }

    protected abstract StorageType getType();
}
