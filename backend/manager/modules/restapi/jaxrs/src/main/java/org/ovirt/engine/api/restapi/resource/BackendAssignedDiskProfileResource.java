package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.resource.AssignedDiskProfileResource;

public class BackendAssignedDiskProfileResource extends AbstractBackendDiskProfileResource implements AssignedDiskProfileResource {

    private final BackendAssignedDiskProfilesResource parent;

    public BackendAssignedDiskProfileResource(String id, BackendAssignedDiskProfilesResource parent) {
        super(id);
        this.parent = parent;
    }

    public BackendAssignedDiskProfilesResource getParent() {
        return parent;
    }

    @Override
    public DiskProfile get() {
        return addLinks(super.get());
    }

    @Override
    protected DiskProfile addParents(DiskProfile diskProfile) {
        return parent.addParents(diskProfile);
    }
}
