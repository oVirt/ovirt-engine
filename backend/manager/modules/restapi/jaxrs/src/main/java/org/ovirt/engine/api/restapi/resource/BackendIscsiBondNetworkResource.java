package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedVnicProfilesResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    // TODO: Replace this with a specific interface for iSCSI bond network.
    implements NetworkResource {

    private Guid bondId;

    public BackendIscsiBondNetworkResource(Guid bondId, String networkId) {
        super(networkId, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
        this.bondId = bondId;
    }

    @Override
    public Network get() {
        IscsiBond bond = getBond();
        if (!bond.getNetworkIds().contains(guid)) {
            notFound();
        }
        return performGet(VdcQueryType.GetNetworkById, new IdQueryParameters(guid));
    }

    @Override
    public Response remove() {
        IscsiBond bond = getBond();
        if (!bond.getNetworkIds().contains(guid)) {
            notFound();
        }
        bond.getNetworkIds().remove(guid);
        return performAction(VdcActionType.EditIscsiBond, new EditIscsiBondParameters(bond));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        // TODO: Remove this method when a specific service is created for iSCSI bond network.
        return null;
    }

    @Override
    public AssignedVnicProfilesResource getVnicProfilesResource() {
        // TODO: Remove this method when a specific service is created for iSCSI bond network.
        return null;
    }

    @Override
    public NetworkLabelsResource getNetworkLabelsResource() {
        // TODO: Remove this method when a specific service is created for iSCSI bond network.
        return null;
    }

    @Override
    protected Network addParents(Network model) {
        return BackendNetworkHelper.addParents(model);
    }

    private IscsiBond getBond() {
        return getEntity(
            IscsiBond.class,
            VdcQueryType.GetIscsiBondById,
            new IdQueryParameters(bondId),
            bondId.toString(),
            true
        );
    }
}
