package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAssignedVnicProfilesResourceTest extends AbstractBackendVnicProfilesResourceTest<BackendAssignedVnicProfilesResource> {

    public BackendAssignedVnicProfilesResourceTest() {
        super(new BackendAssignedVnicProfilesResource(NETWORK_ID.toString()),
                QueryType.GetVnicProfilesByNetworkId,
                IdQueryParameters.class);
    }

    @Override
    protected List<VnicProfile> getCollection() {
        return collection.list().getVnicProfiles();
    }
}
