package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;

public abstract class StorageServerConnectionCommandBase<T extends StorageServerConnectionParametersBase> extends
        CommandBase<T> {
    public StorageServerConnectionCommandBase(T parameters) {
        super(parameters);
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

    protected StorageDomainDAO getStorageDomainDao() {
        return getDbFacade().getStorageDomainDao();
    }


    protected StorageServerConnectionDAO getStorageConnDao() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    protected LunDAO getLunDao() {
        return getDbFacade().getLunDao();
    }

    protected List<StorageDomain> getStorageDomainsByConnId(String connectionId) {
        return getStorageDomainDao().getAllByConnectionId(Guid.createGuidFromString(connectionId));
    }

    protected boolean isConnWithSameDetailsExists(StorageServerConnections connection) {
        List<StorageServerConnections> connections = null;
        if (connection.getstorage_type().isFileDomain()) {
            String connectionField = connection.getconnection();
            connections = getStorageConnDao().getAllForStorage(connectionField);
        }
        else {
            connections = getStorageConnDao().getAllForConnection(connection);
        }

        boolean isDuplicateConnExists = (connections.size() > 1
                || (connections.size() == 1 && !connections.get(0).getid().equalsIgnoreCase(connection.getid())));
        return isDuplicateConnExists;
    }

    protected boolean checkIsConnectionFieldEmpty(StorageServerConnections connection) {
        if (StringUtils.isEmpty(connection.getconnection())) {
            String fieldName = getFieldName(connection);
            addCanDoActionMessage(String.format("$fieldName %1$s", fieldName));
            addCanDoActionMessage(VdcBllMessages.VALIDATION_STORAGE_CONNECTION_EMPTY_CONNECTION);
            return true;
        }
        return false;
    }

    private static String getFieldName(StorageServerConnections paramConnection) {
        String fieldName;
        if (paramConnection.getstorage_type().equals(StorageType.ISCSI)) {
            fieldName = "address";
        } else if (paramConnection.getstorage_type().isFileDomain()) {
            fieldName = "path";
        } else {
            fieldName = "connection";
        }
        return fieldName;
    }
}
