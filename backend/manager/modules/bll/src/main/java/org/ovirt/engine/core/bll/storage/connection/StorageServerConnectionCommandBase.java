package org.ovirt.engine.core.bll.storage.connection;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public abstract class StorageServerConnectionCommandBase<T extends StorageServerConnectionParametersBase> extends
        CommandBase<T> {

    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private ISCSIStorageHelper iscsiStorageHelper;

    protected StorageServerConnectionCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    public StorageServerConnectionCommandBase(Guid commandId) {
        super(commandId);
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

    protected List<StorageDomain> getStorageDomainsByConnId(String connectionId) {
        return storageDomainDao.getAllByConnectionId(Guid.createGuidFromString(connectionId));
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

    protected String isConnWithSameDetailsExists(StorageServerConnections connection, Guid storagePoolId) {
        List<StorageServerConnections> connections = null;
        if (connection.getStorageType() == StorageType.LOCALFS) {
            List<StorageServerConnections> connectionsForPool = storagePoolId == null ? Collections.emptyList() :
                    storageServerConnectionDao.getAllConnectableStorageSeverConnection(storagePoolId);
            List<StorageServerConnections> connectionsForPath = storageServerConnectionDao.getAllForStorage(connection.getConnection());
            connections = (List<StorageServerConnections>) CollectionUtils.intersection(connectionsForPool, connectionsForPath);
        } else if (connection.getStorageType().isFileDomain()) {
            String connectionField = connection.getConnection();
            connections = storageServerConnectionDao.getAllForStorage(connectionField);
        } else {
            StorageServerConnections sameConnection = iscsiStorageHelper.findConnectionWithSameDetails(connection);
            connections =
                    sameConnection != null ? Collections.singletonList(sameConnection)
                            : Collections.emptyList();
        }

        return (connections != null && connections.size() > 0 &&
                connections.get(0) != null &&
                connections.get(0).getId() != null) ? connections.get(0).getId() : "";
    }

    protected String getStorageNameByConnectionId(String connectionId) {
        List<StorageDomain> storageDomainsByConnId = getStorageDomainsByConnId(connectionId);

        if (storageDomainsByConnId != null && storageDomainsByConnId.size() > 0) {
            if (storageDomainsByConnId.get(0).getStorageStaticData() != null) {
                return storageDomainsByConnId.get(0).getStorageStaticData().getName();
            }
        }

        return "";
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
