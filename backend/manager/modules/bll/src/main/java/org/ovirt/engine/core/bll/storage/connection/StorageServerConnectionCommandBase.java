package org.ovirt.engine.core.bll.storage.connection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public abstract class StorageServerConnectionCommandBase<T extends StorageServerConnectionParametersBase> extends
        CommandBase<T> {

    protected StorageServerConnectionCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    protected StorageServerConnections getConnection() {
        return getParameters().getStorageServerConnection();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

    protected StorageServerConnectionDao getStorageConnDao() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    protected List<StorageDomain> getStorageDomainsByConnId(String connectionId) {
        return getStorageDomainDao().getAllByConnectionId(Guid.createGuidFromString(connectionId));
    }

    /**
     * Returns storage pool ID by a specified file domain connection
     * (isn't relevant for block domains as a single connection can be used by multiple block domains).
     */
    protected Guid getStoragePoolIdByFileConnectionId(String connectionId) {
        List<StorageDomain> storageDomains = getStorageDomainsByConnId(connectionId);
        if (storageDomains.isEmpty()) {
            return null;
        }

        return storageDomains.get(0).getStoragePoolId();
    }

    protected boolean isConnWithSameDetailsExists(StorageServerConnections connection, Guid storagePoolId) {
        List<StorageServerConnections> connections = null;
        if (connection.getStorageType() == StorageType.LOCALFS) {
            List<StorageServerConnections> connectionsForPool = storagePoolId == null ? Collections.<StorageServerConnections> emptyList() :
                    getStorageConnDao().getAllConnectableStorageSeverConnection(storagePoolId);
            List<StorageServerConnections> connectionsForPath = getStorageConnDao().getAllForStorage(connection.getConnection());
            connections = (List<StorageServerConnections>) CollectionUtils.intersection(connectionsForPool, connectionsForPath);
        }
        else if (connection.getStorageType().isFileDomain()) {
            String connectionField = connection.getConnection();
            connections = getStorageConnDao().getAllForStorage(connectionField);
        }
        else {
            StorageServerConnections sameConnection = findConnectionWithSameDetails(connection);
            connections =
                    sameConnection != null ? Arrays.asList(sameConnection)
                            : Collections.<StorageServerConnections> emptyList();
        }

        boolean isDuplicateConnExists = connections.size() > 1
                || (connections.size() == 1 && !connections.get(0).getId().equalsIgnoreCase(connection.getId()));
        return isDuplicateConnExists;
    }

    protected StorageServerConnections findConnectionWithSameDetails(StorageServerConnections connection) {
        return ISCSIStorageHelper.findConnectionWithSameDetails(connection);
    }

    protected boolean checkIsConnectionFieldEmpty(StorageServerConnections connection) {
        if (StringUtils.isEmpty(connection.getConnection())) {
            String fieldName = getFieldName(connection);
            addValidationMessageVariable("fieldName", fieldName);
            addValidationMessage(EngineMessage.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
            return true;
        }
        return false;
    }

    private static String getFieldName(StorageServerConnections paramConnection) {
        String fieldName;
        if (paramConnection.getStorageType().equals(StorageType.ISCSI)) {
            fieldName = "address";
        } else if (paramConnection.getStorageType().isFileDomain()) {
            fieldName = "path";
        } else {
            fieldName = "connection";
        }
        return fieldName;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getVds() != null) {
                jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getVds().getName());
            }
        }
        return jobProperties;
    }
}
