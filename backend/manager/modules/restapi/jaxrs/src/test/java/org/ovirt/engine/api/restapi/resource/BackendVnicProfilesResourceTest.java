package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVnicProfilesResourceTest extends AbstractBackendVnicProfilesResourceTest<BackendVnicProfilesResource> {

    public BackendVnicProfilesResourceTest() {
        super(new BackendVnicProfilesResource(), QueryType.GetAllVnicProfiles, QueryParametersBase.class);
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
        setUpEntityQueryExpectations(QueryType.GetNetworkById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NETWORK_ID },
                new Network());
    }
}
