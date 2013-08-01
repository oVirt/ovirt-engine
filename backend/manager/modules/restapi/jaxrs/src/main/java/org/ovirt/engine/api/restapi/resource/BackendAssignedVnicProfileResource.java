package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.resource.AssignedVnicProfileResource;

public class BackendAssignedVnicProfileResource extends AbstractBackendVnicProfileResource implements AssignedVnicProfileResource {

    private BackendAssignedVnicProfilesResource parent;

    public BackendAssignedVnicProfileResource(String id, BackendAssignedVnicProfilesResource parent) {
        super(id);
        this.parent = parent;
    }

    public BackendAssignedVnicProfilesResource getParent() {
        return parent;
    }

    @Override
    public VnicProfile get() {
        return addLinks(super.get());
    }

    @Override
    protected VnicProfile addParents(VnicProfile vnicProfile) {
        return parent.addParents(vnicProfile);
    }

    @Override
    protected VnicProfile doPopulate(VnicProfile model,
            org.ovirt.engine.core.common.businessentities.network.VnicProfile entity) {
        return model;
    }
}
