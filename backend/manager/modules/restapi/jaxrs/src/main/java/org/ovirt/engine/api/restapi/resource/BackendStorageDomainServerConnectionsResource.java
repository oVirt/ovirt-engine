package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionResource;
import org.ovirt.engine.api.resource.StorageDomainServerConnectionsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachStorageConnectionParameters;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainServerConnectionsResource
        extends AbstractBackendCollectionResource<StorageConnection, StorageServerConnections>
        implements StorageDomainServerConnectionsResource {

    Guid storageDomainId = null;

    public BackendStorageDomainServerConnectionsResource(Guid storageDomainId) {
        super(StorageConnection.class, StorageServerConnections.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public StorageConnections list() {
        List<StorageServerConnections> connections = getConnections();
        return mapCollection(connections);
    }

    protected List<StorageServerConnections> getConnections() {
        return getEntity(List.class,
                QueryType.GetStorageServerConnectionsForDomain,
                new IdQueryParameters(storageDomainId),
                "storage domain: id=" + storageDomainId);
    }

    private StorageConnections mapCollection(List<StorageServerConnections> entities) {
        StorageConnections collection = new StorageConnections();
        for (StorageServerConnections entity : entities) {
            StorageConnection connection = map(entity);
            if (connection != null) {
                collection.getStorageConnections().add(addLinks(populate(connection, entity)));
            }
        }
        return collection;
    }

    @Override
    public Response add(StorageConnection storageConnection) {
        validateParameters(storageConnection, "id");

        return performAction(ActionType.AttachStorageConnectionToStorageDomain,
                new AttachDetachStorageConnectionParameters(storageDomainId, storageConnection.getId()));
    }

    @Override
    public StorageDomainServerConnectionResource getConnectionResource(String id) {
        return inject(new BackendStorageDomainServerConnectionResource(id, this));
    }
}
