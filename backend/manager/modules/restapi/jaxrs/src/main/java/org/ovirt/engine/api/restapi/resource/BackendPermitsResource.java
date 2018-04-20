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
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
        return mapCollection(getBackendCollection(QueryType.GetRoleActionGroupsByRoleId,
                                                  new IdQueryParameters(roleId)));
    }

    @Override
    public Response add(Permit permit) {
        validateParameters(permit, "name|id");
        ArrayList<ActionGroup> actionGroups = new ArrayList<>();
        addIfNotExists(actionGroups, map(permit));
        return performCreate(ActionType.AttachActionGroupsToRole,
                               new ActionGroupsToRoleParameter(roleId, actionGroups),
                               new PermitIdResolver(actionGroups.get(0)));
    }

    private void addIfNotExists(List<ActionGroup> list, ActionGroup item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }

    @Override
    public PermitResource getPermitResource(String id) {
        return inject(new BackendPermitResource(id, this));
    }

    public ActionGroup lookupId(String id) {
        try {
            return ActionGroup.forValue(Integer.parseInt(id));
        } catch (NumberFormatException exception) {
            return null;
        }
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
