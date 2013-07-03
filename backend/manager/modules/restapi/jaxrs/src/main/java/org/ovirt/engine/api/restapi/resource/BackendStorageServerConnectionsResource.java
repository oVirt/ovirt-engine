package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionsResource extends AbstractBackendCollectionResource<Storage, StorageServerConnections> implements StorageServerConnectionsResource {
    private final EntityIdResolver<String> ENTITY_RETRIEVER =
            new QueryIdResolver<String>(VdcQueryType.GetStorageServerConnectionById,
                    StorageServerConnectionQueryParametersBase.class);
    private Host host = null; // host used for removal of connection

    public BackendStorageServerConnectionsResource() {
        super(Storage.class, org.ovirt.engine.core.common.businessentities.StorageServerConnections.class);
    }

    @Override
    public StorageConnections list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllStorageServerConnections,
                new VdcQueryParametersBase()));
    }

    @Override
    protected Storage doPopulate(Storage model, StorageServerConnections entity) {
        return model;
    }

    private StorageConnections mapCollection(List<StorageServerConnections> entities) {
        StorageConnections collection = new StorageConnections();
        for (org.ovirt.engine.core.common.businessentities.StorageServerConnections entity : entities) {
            Storage connection = map(entity);
            if (connection != null) {
                collection.getStorageConnections().add(addLinks(populate(connection,entity)));
            }
        }
        return collection;
    }

    @Override
    public Response add(Storage storage) {
        validateParameters(storage, "type");
        // map to backend object
        StorageServerConnections storageConnection =
                getMapper(Storage.class, StorageServerConnections.class).map(storage, null);

        Guid hostId = Guid.Empty;
        if (storage.getHost() != null) {
           hostId = getHostId(storage.getHost());
        }
        switch (storageConnection.getstorage_type()) {
        case ISCSI:
            validateParameters(storage, "address", "target", "port");
            break;
        case NFS:
            validateParameters(storage, "address", "path");
            break;
        case LOCALFS:
            validateParameters(storage, "path");
            break;
        case POSIXFS:
        case GLUSTERFS:
            // address is possible, but is optional, non mandatory
            validateParameters(storage, "path", "vfsType");
            break;
        default:
            break;
        }
        return performCreate(VdcActionType.AddStorageServerConnection,
                getAddParams(storageConnection, hostId),
                ENTITY_RETRIEVER);
    }

    private StorageServerConnectionParametersBase getAddParams(StorageServerConnections entity, Guid hostId) {
        StorageServerConnectionParametersBase params = new StorageServerConnectionParametersBase(entity, hostId);
        params.setVdsId(hostId);
        return params;
    }

    @Override
    public Response remove(String id, Host host) {
        if (host != null) {
            this.host = host;
        }
        return super.remove(id);
    }

    @Override
    protected Response performRemove(String id) {
        StorageServerConnections connection = new StorageServerConnections();
        connection.setid(id);
        Guid hostId = Guid.Empty;
        if(this.host != null) {
            hostId = getHostId(host);
        }
        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId);
        return performAction(VdcActionType.RemoveStorageServerConnection, parameters);
    }

    @Override
    @SingleEntityResource
    public StorageServerConnectionResource getStorageConnectionSubResource(String id) {
        return inject(new BackendStorageServerConnectionResource(id, this));
    }
}
