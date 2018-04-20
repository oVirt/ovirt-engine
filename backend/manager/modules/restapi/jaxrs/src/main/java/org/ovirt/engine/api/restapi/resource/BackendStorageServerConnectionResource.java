package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionResource extends
        AbstractBackendSubResource<StorageConnection, org.ovirt.engine.core.common.businessentities.StorageServerConnections> implements StorageServerConnectionResource {
    private BackendStorageServerConnectionsResource parent;

    public static final String HOST = "host";

    public BackendStorageServerConnectionResource(String id, BackendStorageServerConnectionsResource parent) {
        super(id, StorageConnection.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        return performGet(QueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    @Override
    public StorageConnection update(StorageConnection connection) {
        return performUpdate(connection,
                new QueryIdResolver<>(QueryType.GetStorageServerConnectionById,
                        StorageServerConnectionQueryParametersBase.class),
                ActionType.UpdateStorageServerConnection,
                new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(id);

        String host = ParametersHelper.getParameter(httpHeaders, uriInfo, HOST);
        Guid hostId = Guid.Empty;
        if (host != null) {
            hostId = getHostId(host);
        }
        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId, false);
        return performAction(ActionType.RemoveStorageServerConnection, parameters);
    }

    private Guid getHostId(String host) {
        try {
            return Guid.createGuidFromString(host);
        } catch(IllegalArgumentException exception) {
            VdsStatic entity = getEntity(
                VdsStatic.class,
                QueryType.GetVdsStaticByName,
                new NameQueryParameters(host),
                host
            );
            if (entity != null) {
                return entity.getId();
            }
            return Guid.Empty;
        }
    }

    public BackendStorageServerConnectionsResource getParent() {
        return parent;
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<StorageConnection, StorageServerConnections> {
        @Override
        public ActionParametersBase getParameters(StorageConnection incoming, StorageServerConnections entity) {
            StorageServerConnections connection = map(incoming, entity);
            Guid hostId = Guid.Empty;
            if (incoming.getHost() != null) {
               hostId = getHostId(incoming.getHost());
            }
            return new StorageServerConnectionParametersBase(connection, hostId, isForce());
        }

        private Guid getHostId(Host host) {
            // presence of host ID or name already validated
            return host.isSetId()
                    ? new Guid(host.getId())
                    : host.isSetName()
                            ? getEntity(VdsStatic.class,
                                    QueryType.GetVdsStaticByName,
                                    new NameQueryParameters(host.getName()),
                                    "Hosts: name=" + host.getName()).getId()
                            : null;
        }
    }
}
