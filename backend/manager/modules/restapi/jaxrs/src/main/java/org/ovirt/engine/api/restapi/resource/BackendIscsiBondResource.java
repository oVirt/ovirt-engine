package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendIscsiBondsResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendIscsiBondResource
    extends AbstractBackendActionableResource<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond>
    implements IscsiBondResource {

    public  BackendIscsiBondResource(String id) {
        super(id, IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class, SUB_COLLECTIONS);
    }

    @Override
    public IscsiBond get() {
        return performGet(VdcQueryType.GetIscsiBondById, new IdQueryParameters(guid));
    }

    @Override
    public IscsiBond update(IscsiBond iscsiBond) {
        return performUpdate(iscsiBond,
                new QueryIdResolver<>(VdcQueryType.GetIscsiBondById, IdQueryParameters.class),
                VdcActionType.EditIscsiBond,
                new ParametersProvider<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond>() {
                    @Override
                    public VdcActionParametersBase getParameters(IscsiBond incoming, org.ovirt.engine.core.common.businessentities.IscsiBond entity) {
                        return new EditIscsiBondParameters(
                                getMapper(modelType, org.ovirt.engine.core.common.businessentities.IscsiBond.class).map(incoming, entity)
                        );
                    }
                });
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
        return performAction(VdcActionType.RemoveIscsiBond, new RemoveIscsiBondParameters(guid));
    }
}
