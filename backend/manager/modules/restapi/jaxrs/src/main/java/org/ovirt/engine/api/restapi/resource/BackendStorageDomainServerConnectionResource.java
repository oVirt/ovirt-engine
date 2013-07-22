package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionResource;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionsResource;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainServerConnectionResource extends
        AbstractBackendSubResource<StorageConnection, StorageServerConnections> implements StorageDomainServerConnectionResource {
    private StorageDomainServerConnectionsResource parent;

    public BackendStorageDomainServerConnectionResource(String id, StorageDomainServerConnectionsResource parent) {
        super(id, StorageConnection.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        return performGet(VdcQueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    @Override
    protected StorageConnection doPopulate(StorageConnection model, StorageServerConnections entity) {
        return model;
    }

    public StorageDomainServerConnectionsResource getParent() {
        return parent;
    }

}
