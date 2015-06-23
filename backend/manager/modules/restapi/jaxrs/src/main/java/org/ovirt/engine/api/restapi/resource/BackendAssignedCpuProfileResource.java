package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.resource.AssignedCpuProfileResource;

public class BackendAssignedCpuProfileResource extends AbstractBackendCpuProfileResource implements AssignedCpuProfileResource {

    private final BackendAssignedCpuProfilesResource parent;

    public BackendAssignedCpuProfileResource(String id, BackendAssignedCpuProfilesResource parent) {
        super(id);
        this.parent = parent;
    }

    public BackendAssignedCpuProfilesResource getParent() {
        return parent;
    }

    @Override
    public CpuProfile get() {
        return addLinks(super.get());
    }

    @Override
    protected CpuProfile addParents(CpuProfile cpuProfile) {
        return parent.addParents(cpuProfile);
    }
}
