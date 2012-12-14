package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;

public abstract class BaseFsStorageHelper extends StorageHelperBase {

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        boolean returnValue = false;
        StorageServerConnections connection = DbFacade.getInstance().getStorageServerConnectionDao().get(
                storageDomain.getstorage());
        if (connection != null) {
            returnValue = Backend
                    .getInstance()
                    .runInternalAction(VdcActionType.forValue(type),
                            new StorageServerConnectionParametersBase(connection, vdsId)).getSucceeded();
        } else {
            getLog().warn("Did not connect host: " + vdsId + " to storage domain: " + storageDomain.getstorage_name()
                    + " because connection for connectionId:" + storageDomain.getstorage() + " is null.");
        }
        return returnValue;
    }

    @Override
    public boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<StorageServerConnections> connections,
            Guid storagePoolId) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> validateConnections = (HashMap<String, String>) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ValidateStorageServerConnection,
                        new ConnectStorageServerVDSCommandParameters(vds.getId(), storagePoolId, getType(),
                                connections)).getReturnValue();
        return IsConnectSucceeded(validateConnections, connections);
    }

    @Override
    public boolean IsConnectSucceeded(Map<String, String> returnValue,
            List<StorageServerConnections> connections) {
        boolean result = true;
        for (Map.Entry<String, String> entry : returnValue.entrySet()) {
            if (!"0".equals(entry.getValue())) {
                String connectionField = addToAuditLogErrorMessage(entry.getKey(), entry.getValue(), connections);
                printLog(getLog(), connectionField, entry.getValue());
                result = false;
            }
        }

        return result;
    }

    @Override
    public List<StorageServerConnections> GetStorageServerConnectionsByDomain(
            StorageDomainStatic storageDomain) {
        return new ArrayList<StorageServerConnections>(
                Arrays.asList(new StorageServerConnections[] { DbFacade.getInstance()
                        .getStorageServerConnectionDao().get(storageDomain.getstorage()) }));
    }

    @Override
    public boolean StorageDomainRemoved(StorageDomainStatic storageDomain) {
        StorageServerConnections connection =
                DbFacade.getInstance().getStorageServerConnectionDao().get(storageDomain.getstorage());

        if (connection != null) {
            DbFacade.getInstance().getStorageServerConnectionDao().remove(connection.getid());
        }

        return true;
    }

    protected abstract Log getLog();

    protected abstract StorageType getType();
}
