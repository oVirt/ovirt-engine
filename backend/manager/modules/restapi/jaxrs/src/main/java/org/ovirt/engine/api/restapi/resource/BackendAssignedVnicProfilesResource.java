package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.api.resource.AssignedVnicProfileResource;
import org.ovirt.engine.api.resource.AssignedVnicProfilesResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedVnicProfilesResource extends AbstractBackendVnicProfilesResource implements AssignedVnicProfilesResource {

    private String networkId;

    public BackendAssignedVnicProfilesResource(String networkId) {
        super();
        this.networkId = networkId;
    }

    @Override
    public VnicProfiles list() {
        return performList(LinkHelper.NO_PARENT);
    }

    @Override
    public Response add(VnicProfile vnicProfile) {
        if (!vnicProfile.isSetNetwork() || !vnicProfile.getNetwork().isSetId()) {
            vnicProfile.setNetwork(new Network());
            vnicProfile.getNetwork().setId(networkId);
        }

        return super.add(vnicProfile);
    }

    @Override
    protected void validateParameters(VnicProfile vnicProfile) {
        validateParameters(vnicProfile, "name");
    }

    @Override
    public AssignedVnicProfileResource getProfileResource(String id) {
        return inject(new BackendAssignedVnicProfileResource(id, this));
    }

    @Override
    public VnicProfile addParents(VnicProfile vnicProfile) {
        vnicProfile.setNetwork(new Network());
        vnicProfile.getNetwork().setId(networkId);
        return vnicProfile;
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection() {
        return getBackendCollection(QueryType.GetVnicProfilesByNetworkId, new IdQueryParameters(asGuid(networkId)));
    }
}
