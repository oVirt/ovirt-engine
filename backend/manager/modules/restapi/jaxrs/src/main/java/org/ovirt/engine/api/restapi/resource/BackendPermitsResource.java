package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendPermitsResource
        extends AbstractBackendCollectionResource<Permit, ActionGroup>
        implements PermitsResource {

    protected Guid roleId;

    public BackendPermitsResource(Guid roleId) {
        super(Permit.class, ActionGroup.class);
        this.roleId = roleId;
    }

    @Override
    public Permits list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetRoleActionGroupsByRoleId,
                                                  new MultilevelAdministrationByRoleIdParameters(roleId)));
    }

    @Override
    public Response add(Permit permit) {
        validateParameters(permit, "name|id");
        validateEnums(Permit.class, permit);
        ArrayList<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        actionGroups.add(map(permit));
        return performCreation(VdcActionType.AttachActionGroupsToRole,
                               new ActionGroupsToRoleParameter(roleId, actionGroups),
                               new PermitIdResolver(actionGroups.get(0)));
    }

    @Override
    public Response performRemove(String id) {
        ActionGroup entity = lookupId(id);
        if (entity == null) {
            notFound();
            return null;
        }
        return performAction(VdcActionType.DetachActionGroupsFromRole,
                             new ActionGroupsToRoleParameter(roleId, asList(entity)));
    }

    @Override
    @SingleEntityResource
    public PermitResource getPermitSubResource(String id) {
        return inject(new BackendPermitResource(id, this));
    }

    public ActionGroup lookupId(String id) {
        return getMapper(String.class, ActionGroup.class).map(id, null);
    }

    protected Permits mapCollection(List<ActionGroup> entities) {
        Permits collection = new Permits();
        for (ActionGroup entity : entities) {
            collection.getPermits().add(addLinks(map(entity)));
        }
        return collection;
    }

    @Override
    public Permit addParents(Permit permit) {
        permit.setRole(new Role());
        permit.getRole().setId(roleId.toString());
        return permit;
    }

    protected class PermitIdResolver extends EntityIdResolver<Guid> {

        private ActionGroup actionGroup;

        PermitIdResolver(ActionGroup actionGroup) {
            this.actionGroup = actionGroup;
        }

        @Override
        public ActionGroup lookupEntity(Guid id) {
            return actionGroup;
        }
    }

}
