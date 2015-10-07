package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVnicProfilesResource extends AbstractBackendVnicProfilesResource implements VnicProfilesResource {

    @Override
    public VnicProfiles list() {
        return performList();
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.network.VnicProfile> getVnicProfilesCollection() {
        return getBackendCollection(VdcQueryType.GetAllVnicProfiles, new VdcQueryParametersBase());
    }

    public Response add(VnicProfile vnicProfile) {
        return super.add(vnicProfile);
    }

    @Override
    protected void validateParameters(VnicProfile vnicProfile) {
        validateParameters(vnicProfile, "name", "network.id");
        String networkId = vnicProfile.getNetwork().getId();
        // verify the network.id is well provided
        getEntity(Network.class, VdcQueryType.GetNetworkById, new IdQueryParameters(asGuid(networkId)), "Network: id="
                + networkId);
    }

    @Override
    public VnicProfileResource getProfileResource(String id) {
        return inject(new BackendVnicProfileResource(id));
    }
}
