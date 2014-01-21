package org.ovirt.engine.core.bll.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@InternalCommandAttribute
public abstract class ConnectHostToStoragePoolServerCommandBase<T extends StoragePoolParametersBase> extends
        StorageHandlingCommandBase<T> {
    private List<StorageServerConnections> _connections;
    private Map<StorageType, List<StorageServerConnections>> connectionsTypeMap;

    public ConnectHostToStoragePoolServerCommandBase(T parameters) {
        super(parameters);
    }

    protected Map<StorageType, List<StorageServerConnections>> getConnectionsTypeMap() {
        return connectionsTypeMap;
    }

    protected void initConnectionList() {
        _connections = DbFacade.getInstance().getStorageServerConnectionDao().getAllConnectableStorageSeverConnection(getStoragePool().getId());
        updateConnectionsTypeMap();
    }

    private void updateConnectionsTypeMap() {
        connectionsTypeMap = new HashMap<>();
        for (StorageServerConnections conn : _connections) {
            StorageType connType = conn.getstorage_type();
            MultiValueMapUtils.addToMap(connType, conn, connectionsTypeMap);
        }
    }
}
