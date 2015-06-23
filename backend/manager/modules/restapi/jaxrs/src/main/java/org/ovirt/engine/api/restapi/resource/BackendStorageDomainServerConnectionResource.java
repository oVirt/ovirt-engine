package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionResource;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainServerConnectionResource extends
        AbstractBackendSubResource<StorageConnection, StorageServerConnections> implements StorageDomainServerConnectionResource {
    protected BackendStorageDomainServerConnectionsResource parent;

    public BackendStorageDomainServerConnectionResource(String id, BackendStorageDomainServerConnectionsResource parent) {
        super(id, StorageConnection.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        return performGet(VdcQueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    public BackendStorageDomainServerConnectionsResource getParent() {
        return parent;
    }

    @Override
    @DELETE
    public Response remove() {
        get();
        AttachDetachStorageConnectionParameters params =
                new AttachDetachStorageConnectionParameters(parent.storageDomainId, id);

        return performAction(VdcActionType.DetachStorageConnectionFromStorageDomain, params);
    }

}
