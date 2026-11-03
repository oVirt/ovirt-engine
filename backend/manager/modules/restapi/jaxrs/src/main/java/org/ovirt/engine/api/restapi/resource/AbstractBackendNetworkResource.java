package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.restapi.util.FieldCleaner;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;

public abstract class AbstractBackendNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

    protected AbstractBackendNetworksResource parent;
    private ActionType removeAction;

    public AbstractBackendNetworkResource(
            String id,
            AbstractBackendNetworksResource parent,
            ActionType removeAction) {
        super(id, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
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

    protected void removeRestrictedInfo(Network network) {
        // Filtered users are not allowed to view restricted information
        if (!isAdmin()) {
            nullifyRestrictedFields(network);
        }
    }

    AbstractBackendNetworksResource getParent() {
        return parent;
    }

    protected abstract ActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity);

    public Response remove() {
        get();
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(asGuidOr404(id));
        if (entity == null) {
            notFound();
            return null;
        }
        return performAction(removeAction, getRemoveParameters(entity));
    }

    public static void nullifyRestrictedFields(Network network) {
        FieldCleaner.nullifyAllFieldsExcept(network, "id", "name", "dataCenter");
        FieldCleaner.nullifyAllFieldsExcept(network.getDataCenter(), "id");
    }
}
