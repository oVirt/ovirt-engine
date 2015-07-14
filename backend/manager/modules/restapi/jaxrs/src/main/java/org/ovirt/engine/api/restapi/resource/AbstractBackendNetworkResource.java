package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public abstract class AbstractBackendNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

    protected AbstractBackendNetworksResource parent;
    private VdcActionType removeAction;

    public AbstractBackendNetworkResource(
            String id,
            AbstractBackendNetworksResource parent,
            VdcActionType removeAction,
            String... subCollections) {
        super(id, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class, subCollections);
        this.parent = parent;
        this.removeAction = removeAction;
    }

    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    AbstractBackendNetworksResource getParent() {
        return parent;
    }

    protected abstract VdcActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity);

    public Response remove() {
        get();
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(asGuidOr404(id));
        if (entity == null) {
            notFound();
            return null;
        }
        return performAction(removeAction, getRemoveParameters(entity));
    }
}
