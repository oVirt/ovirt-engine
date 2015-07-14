package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendIscsiBondNetworkResource extends BackendNetworkResource {

    private BackendIscsiBondNetworksResource parent;

    public BackendIscsiBondNetworkResource(String id, BackendIscsiBondNetworksResource parent) {
        super(id, parent);
        this.parent = parent;
    }

    @Override
    public Network get() {
        IscsiBond iscsiBond = parent.getIscsiBond();

        if (!iscsiBond.getNetworkIds().contains(guid)) {
            return notFound();
        }

        org.ovirt.engine.core.common.businessentities.network.Network entity = getNetwork();

        if (entity == null) {
            return notFound();
        }

        return addLinks(map(entity));
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetwork() {
        return getEntity(org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById, new IdQueryParameters(guid), guid.toString());
    }

    @Override
    public BackendIscsiBondNetworksResource getParent() {
        return parent;
    }


    @Override
    public Response remove() {
        get();
        IscsiBond iscsiBond = parent.getIscsiBond();
        iscsiBond.getNetworkIds().remove(guid);
        return performAction(VdcActionType.EditIscsiBond, new EditIscsiBondParameters(iscsiBond));
    }
}
