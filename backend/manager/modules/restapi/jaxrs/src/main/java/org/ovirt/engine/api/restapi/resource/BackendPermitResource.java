package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class BackendPermitResource
    extends AbstractBackendResource<Permit, ActionGroup>
    implements PermitResource {

    protected String id;
    protected BackendPermitsResource parent;

    public BackendPermitResource(String id, BackendPermitsResource parent) {
        super(Permit.class, ActionGroup.class);
        this.id = id;
        this.parent = parent;
    }

    public BackendPermitsResource getParent() {
        return parent;
    }

    @Override
    public Permit get() {
        ActionGroup entity = parent.lookupId(id);
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    @Override
    protected Permit addParents(Permit permit) {
        return parent.addParents(permit);
    }

    @Override
    protected Permit doPopulate(Permit model, ActionGroup entity) {
        return model;
    }
}
