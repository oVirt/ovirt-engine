package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendIscsiBondResource
    extends AbstractBackendActionableResource<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond>
    implements IscsiBondResource {

    public  BackendIscsiBondResource(String id) {
        super(id, IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class);
    }

    @Override
    public IscsiBond get() {
        return performGet(QueryType.GetIscsiBondById, new IdQueryParameters(guid));
    }

    @Override
    public IscsiBond update(IscsiBond iscsiBond) {
        return performUpdate(iscsiBond,
                new QueryIdResolver<>(QueryType.GetIscsiBondById, IdQueryParameters.class),
                ActionType.EditIscsiBond,
                (incoming, entity) -> new EditIscsiBondParameters(
                        getMapper(modelType, org.ovirt.engine.core.common.businessentities.IscsiBond.class).map(incoming, entity)
                ));
    }

    @Override
    public NetworksResource getNetworksResource() {
        return inject(new BackendIscsiBondNetworksResource(id));
    }

    @Override
    public StorageServerConnectionsResource getStorageServerConnectionsResource() {
        return inject(new BackendIscsiBondStorageConnectionsResource(id));
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveIscsiBond, new RemoveIscsiBondParameters(guid));
    }
}
