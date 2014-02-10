package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVnicProfilesResourceTest extends AbstractBackendVnicProfilesResourceTest<BackendVnicProfilesResource> {

    public BackendVnicProfilesResourceTest() {
        super(new BackendVnicProfilesResource(), VdcQueryType.GetAllVnicProfiles, VdcQueryParametersBase.class);
    }

    @Override
    protected String[] getIncompleteFields() {
        return new String[] { "network.id" };
    }

    @Override
    protected VnicProfile createIncompleteVnicProfile() {
        VnicProfile vnicProfile = super.createIncompleteVnicProfile();
        vnicProfile.setName(NAMES[0]);
        return vnicProfile;
    }

    @Override
    protected List<VnicProfile> getCollection() {
        return collection.list().getVnicProfiles();
    }

    @Override
    protected void setUpNetworkQueryExpectations() {
        setUpEntityQueryExpectations(VdcQueryType.GetNetworkById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NETWORK_ID },
                new Network());
    }
}
