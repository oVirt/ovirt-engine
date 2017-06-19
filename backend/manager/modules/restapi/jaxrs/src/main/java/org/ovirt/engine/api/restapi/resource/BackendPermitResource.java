package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class BackendPermitResource
        extends AbstractBackendSubResource<Permit, ActionGroup>
    implements PermitResource {

    protected BackendPermitsResource parent;

    public BackendPermitResource(String id, BackendPermitsResource parent) {
        super(id, Permit.class, ActionGroup.class);
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
    public Response remove() {
        get();
        ActionGroup entity = parent.lookupId(id);
        if (entity == null) {
            notFound();
            return null;
        }
        return performAction(ActionType.DetachActionGroupsFromRole,
                new ActionGroupsToRoleParameter(parent.roleId, asList(entity)));
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // Permit ID is not a GUID
        return null;
    }
}
