package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageServerConnectionResource extends
        AbstractBackendSubResource<Storage, org.ovirt.engine.core.common.businessentities.StorageServerConnections> implements StorageServerConnectionResource {
    private BackendStorageServerConnectionsResource parent;

    public BackendStorageServerConnectionResource(String id, BackendStorageServerConnectionsResource parent) {
        super(id, Storage.class, StorageServerConnections.class);
        this.parent = parent;
    }

    @Override
    public Storage get() {
        return performGet(VdcQueryType.GetStorageServerConnectionById,
                new StorageServerConnectionQueryParametersBase(guid.toString()));
    }

    @Override
    public Storage update(Storage connection) {
        validateEnums(Storage.class, connection);
        return performUpdate(connection,
                new QueryIdResolver<String>(VdcQueryType.GetStorageServerConnectionById,
                        StorageServerConnectionQueryParametersBase.class),
                VdcActionType.UpdateStorageServerConnection,
                new UpdateParametersProvider());
    }

    @Override
    protected Storage doPopulate(Storage model, StorageServerConnections entity) {
        return model;
    }

    public BackendStorageServerConnectionsResource getParent() {
        return parent;
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<Storage, StorageServerConnections> {
        @Override
        public VdcActionParametersBase getParameters(Storage incoming, StorageServerConnections entity) {
            StorageServerConnections connection = map(incoming, entity);
            Guid hostId = Guid.Empty;
            if (incoming.getHost() != null) {
               hostId = getHostId(incoming.getHost());
            }
            return new StorageServerConnectionParametersBase(connection, hostId);
        }

        private Guid getHostId(Host host) {
            // presence of host ID or name already validated
            return host.isSetId()
                    ? new Guid(host.getId())
                    : host.isSetName()
                            ? getEntity(VDS.class,
                                    SearchType.VDS,
                                    "Hosts: name=" + host.getName()).getId()
                            : null;
        }
    }
}
