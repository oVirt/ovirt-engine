package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendAssignedCpuProfilesResourceTest extends AbstractBackendCpuProfilesResourceTest<BackendAssignedCpuProfilesResource> {

    public BackendAssignedCpuProfilesResourceTest() {
        super(new BackendAssignedCpuProfilesResource(CLUSTER_ID.toString()), QueryType.GetCpuProfilesByClusterId);
    }

    @Override
    protected List<CpuProfile> getCollection() {
        return collection.list().getCpuProfiles();
    }
}
