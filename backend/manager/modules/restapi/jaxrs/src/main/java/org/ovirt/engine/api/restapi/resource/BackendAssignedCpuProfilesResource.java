package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.CpuProfiles;
import org.ovirt.engine.api.resource.AssignedCpuProfileResource;
import org.ovirt.engine.api.resource.AssignedCpuProfilesResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendAssignedCpuProfilesResource extends AbstractBackendCpuProfilesResource implements AssignedCpuProfilesResource {

    private final String clusterId;

    public BackendAssignedCpuProfilesResource(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public CpuProfiles list() {
        return performList();
    }

    @Override
    public Response add(CpuProfile cpuProfile) {
        if (!cpuProfile.isSetCluster() || !cpuProfile.getCluster().isSetId()) {
            cpuProfile.setCluster(new Cluster());
            cpuProfile.getCluster().setId(clusterId);
        }

        return super.add(cpuProfile);
    }

    @Override
    protected void validateParameters(CpuProfile cpuProfile) {
        validateParameters(cpuProfile, "name");
    }

    @Override
    public AssignedCpuProfileResource getProfileResource(String id) {
        return inject(new BackendAssignedCpuProfileResource(id, this));
    }

    @Override
    public CpuProfile addParents(CpuProfile cpuProfile) {
        cpuProfile.setCluster(new Cluster());
        cpuProfile.getCluster().setId(clusterId);
        return cpuProfile;
    }

    @Override
    protected List<org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> getCpuProfilesCollection() {
        return getBackendCollection(QueryType.GetCpuProfilesByClusterId,
                new IdQueryParameters(asGuidOr404(clusterId)));
    }
}
