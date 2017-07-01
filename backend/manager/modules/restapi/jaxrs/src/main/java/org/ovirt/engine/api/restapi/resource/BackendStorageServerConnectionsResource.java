package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionsResource
        extends AbstractBackendCollectionResource<StorageConnection, StorageServerConnections>
        implements StorageServerConnectionsResource {

    private final EntityIdResolver<String> ENTITY_RETRIEVER =
            new QueryIdResolver<>(QueryType.GetStorageServerConnectionById,
                    StorageServerConnectionQueryParametersBase.class);

    public BackendStorageServerConnectionsResource() {
        super(StorageConnection.class, org.ovirt.engine.core.common.businessentities.StorageServerConnections.class);
    }

    @Override
    public StorageConnections list() {
        return mapCollection(getBackendCollection(QueryType.GetAllStorageServerConnections,
                new QueryParametersBase()));
    }

    private StorageConnections mapCollection(List<StorageServerConnections> entities) {
        StorageConnections collection = new StorageConnections();
        for (org.ovirt.engine.core.common.businessentities.StorageServerConnections entity : entities) {
            StorageConnection connection = map(entity);
            if (connection != null) {
                collection.getStorageConnections().add(addLinks(populate(connection, entity)));
            }
        }
        return collection;
    }

    @Override
    public Response add(StorageConnection storageConn) {
        validateParameters(storageConn, "type");
        // map to backend object
        StorageServerConnections storageConnection =
                getMapper(StorageConnection.class, StorageServerConnections.class).map(storageConn, null);

        Guid hostId = Guid.Empty;
        if (storageConn.getHost() != null) {
           hostId = getHostId(storageConn.getHost());
        }
        switch (storageConnection.getStorageType()) {
        case ISCSI:
            validateParameters(storageConn, "address", "target", "port");
            break;
        case NFS:
            validateParameters(storageConn, "address", "path");
            break;
        case LOCALFS:
            validateParameters(storageConn, "path");
            break;
        case POSIXFS:
        case GLUSTERFS:
            // address is possible, but is optional, non mandatory
            validateParameters(storageConn, "path", "vfsType");
            break;
        default:
            break;
        }
        return performCreate(ActionType.AddStorageServerConnection,
                getAddParams(storageConnection, hostId),
                ENTITY_RETRIEVER);
    }

    private StorageServerConnectionParametersBase getAddParams(StorageServerConnections entity, Guid hostId) {
        StorageServerConnectionParametersBase params = new StorageServerConnectionParametersBase(entity, hostId, false);
        params.setVdsId(hostId);
        return params;
    }

    @Override
    public StorageServerConnectionResource getStorageConnectionResource(String id) {
        return inject(new BackendStorageServerConnectionResource(id, this));
    }
}
