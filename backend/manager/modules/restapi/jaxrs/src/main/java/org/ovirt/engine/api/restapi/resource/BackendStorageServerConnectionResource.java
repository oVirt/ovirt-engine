package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionResource extends
        AbstractBackendSubResource<StorageConnection, org.ovirt.engine.core.common.businessentities.StorageServerConnections> implements StorageServerConnectionResource {
    private BackendStorageServerConnectionsResource parent;

    public BackendStorageServerConnectionResource(String id, BackendStorageServerConnectionsResource parent) {
        super(id, StorageConnection.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public StorageConnection get() {
        return performGet(VdcQueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    @Override
    public StorageConnection update(StorageConnection connection) {
        validateEnums(StorageConnection.class, connection);
        return performUpdate(connection,
                new QueryIdResolver<String>(VdcQueryType.GetStorageServerConnectionById,
                        StorageServerConnectionQueryParametersBase.class),
                VdcActionType.UpdateStorageServerConnection,
                new UpdateParametersProvider());
    }

    @Override
    public Response remove(Action action) {
        get();
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(id);
        Guid hostId = Guid.Empty;

        if (action != null && action.isSetHost()) {
            hostId = getHostId(action.getHost());
        }


        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId, false);
        return performAction(VdcActionType.RemoveStorageServerConnection, parameters);
    }

    public BackendStorageServerConnectionsResource getParent() {
        return parent;
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<StorageConnection, StorageServerConnections> {
        @Override
        public VdcActionParametersBase getParameters(StorageConnection incoming, StorageServerConnections entity) {
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
                                    VdcQueryType.GetVdsStaticByName,
                                    new NameQueryParameters(host.getName()),
                                    "Hosts: name=" + host.getName()).getId()
                            : null;
        }
    }

    @Override
    public Response remove() {
        get();
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(id);
        Guid hostId = Guid.Empty;
        StorageServerConnectionParametersBase parameters =
                new StorageServerConnectionParametersBase(connection, hostId, false);
        return performAction(VdcActionType.RemoveStorageServerConnection, parameters);
    }
}
