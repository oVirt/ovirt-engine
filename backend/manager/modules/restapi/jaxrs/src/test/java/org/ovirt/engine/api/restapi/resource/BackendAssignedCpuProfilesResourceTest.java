package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedCpuProfilesResourceTest extends AbstractBackendCpuProfilesResourceTest<BackendAssignedCpuProfilesResource> {

    public BackendAssignedCpuProfilesResourceTest() {
        super(new BackendAssignedCpuProfilesResource(CLUSTER_ID.toString()), QueryType.GetCpuProfilesByClusterId);
    }

    @Override
    protected List<CpuProfile> getCollection() {
        return collection.list().getCpuProfiles();
    }
}
