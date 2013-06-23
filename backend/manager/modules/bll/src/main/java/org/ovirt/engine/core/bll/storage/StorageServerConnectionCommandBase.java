package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;

public abstract class StorageServerConnectionCommandBase<T extends StorageServerConnectionParametersBase> extends
        StorageHandlingCommandBase<T> {
    public StorageServerConnectionCommandBase(T parameters) {
        super(parameters);
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
}
