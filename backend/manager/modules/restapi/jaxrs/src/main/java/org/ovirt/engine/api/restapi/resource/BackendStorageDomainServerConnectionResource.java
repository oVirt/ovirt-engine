package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

public class BackendStorageDomainServerConnectionResource extends
        AbstractBackendSubResource<StorageConnection, StorageServerConnections> implements StorageDomainServerConnectionResource {
    protected BackendStorageDomainServerConnectionsResource parent;

    public BackendStorageDomainServerConnectionResource(String id, BackendStorageDomainServerConnectionsResource parent) {
        super(id, StorageConnection.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        return performGet(QueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    public BackendStorageDomainServerConnectionsResource getParent() {
        return parent;
    }

    @Override
    public Response remove() {
        get();
        AttachDetachStorageConnectionParameters params =
                new AttachDetachStorageConnectionParameters(parent.storageDomainId, id);

        return performAction(ActionType.DetachStorageConnectionFromStorageDomain, params);
    }

}
