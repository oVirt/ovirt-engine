package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

@InternalCommandAttribute
public abstract class ConnectHostToStoragePoolServerCommandBase<T extends StoragePoolParametersBase> extends
        StorageHandlingCommandBase<T> {
    private List<StorageServerConnections> _connections;
    private Map<StorageType, List<StorageServerConnections>> connectionsTypeMap;

    public ConnectHostToStoragePoolServerCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ConnectHostToStoragePoolServerCommandBase(T parameters) {
        super(parameters);
    }

    protected Map<StorageType, List<StorageServerConnections>> getConnectionsTypeMap() {
        return connectionsTypeMap;
    }

    protected void initConnectionList(boolean includeInactiveDomains) {
        Set<StorageDomainStatus> statuses;

        statuses = includeInactiveDomains ?
                EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Unknown, StorageDomainStatus.Inactive) :
                EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Unknown);

        _connections =
                DbFacade.getInstance()
                        .getStorageServerConnectionDao()
                        .getStorageConnectionsByStorageTypeAndStatus(getStoragePool().getId(),
                                null,
                                statuses);
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
