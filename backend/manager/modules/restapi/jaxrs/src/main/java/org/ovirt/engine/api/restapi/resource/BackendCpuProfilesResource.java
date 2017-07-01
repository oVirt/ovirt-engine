package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.CpuProfiles;
import org.ovirt.engine.api.resource.CpuProfileResource;
import org.ovirt.engine.api.resource.CpuProfilesResource;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendCpuProfilesResource extends AbstractBackendCpuProfilesResource implements CpuProfilesResource {

    @Override
    public CpuProfiles list() {
        return performList();
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> getCpuProfilesCollection() {
        return getBackendCollection(QueryType.GetAllCpuProfiles, new QueryParametersBase());
    }

    @Override
    public Response add(CpuProfile cpuProfile) {
        return super.add(cpuProfile);
    }

    @Override
    protected void validateParameters(CpuProfile cpuProfile) {
        validateParameters(cpuProfile, "name", "cluster.id");
        String clusterId = cpuProfile.getCluster().getId();
        // verify the cluster.id is well provided
        getEntity(Cluster.class,
                QueryType.GetClusterById,
                new IdQueryParameters(asGuid(clusterId)),
                "cluster: id="
                        + clusterId);
    }

    @Override
    public CpuProfileResource getProfileResource(String id) {
        return inject(new BackendCpuProfileResource(id));
    }
}
