package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.restapi.types.NetworkMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondNetworksResource extends BackendNetworksResource {

    private Guid iscsiBondId;

    public BackendIscsiBondNetworksResource(String iscsiBondId) {
        super(QueryType.GetNetworksByIscsiBondId);
        this.iscsiBondId = Guid.createGuidFromString(iscsiBondId);
    }

    @Override
    public Networks list() {
        return mapCollection(
                getBackendCollection(QueryType.GetNetworksByIscsiBondId, new IdQueryParameters(iscsiBondId)),
                LinkHelper.NO_PARENT
        );
    }

    @Override
    public Response add(Network network) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = NetworkMapper.map(network, null);

        IscsiBond iscsiBond = getIscsiBond();
        iscsiBond.getNetworkIds().add(entity.getId());
        return performAction(ActionType.EditIscsiBond, new EditIscsiBondParameters(iscsiBond));
    }

    @Override
    public NetworkResource getNetworkResource(String id) {
        return inject(new BackendIscsiBondNetworkResource(id, this));
    }

    public IscsiBond getIscsiBond() {
        return getEntity(IscsiBond.class, QueryType.GetIscsiBondById, new IdQueryParameters(iscsiBondId), iscsiBondId.toString());
    }
}
