package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendCpuProfilesResourceTest extends AbstractBackendCpuProfilesResourceTest<BackendCpuProfilesResource> {

    public BackendCpuProfilesResourceTest() {
        super(new BackendCpuProfilesResource(), QueryType.GetAllCpuProfiles);
    }

    @Override
    protected String[] getIncompleteFields() {
        return new String[] { "cluster.id" };
    }

    @Override
    protected CpuProfile createIncompleteCpuProfile() {
        CpuProfile cpuProfile = super.createIncompleteCpuProfile();
        cpuProfile.setName(NAMES[0]);
        return cpuProfile;
    }

    @Override
    protected List<CpuProfile> getCollection() {
        return collection.list().getCpuProfiles();
    }

    @Override
    protected void setUpClusterQueryExpectations() {
        setUpEntityQueryExpectations(QueryType.GetClusterById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { CLUSTER_ID },
                new Cluster());
    }
}
