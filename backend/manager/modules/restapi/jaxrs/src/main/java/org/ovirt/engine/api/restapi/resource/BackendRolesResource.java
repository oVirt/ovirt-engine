package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.api.restapi.types.Mapper;

public class BackendRolesResource
        extends AbstractBackendCollectionResource<Role, roles>
        implements RolesResource {

    static final String[] SUB_COLLECTIONS = { "permits" };

    public BackendRolesResource() {
        super(Role.class, roles.class, SUB_COLLECTIONS);
    }

    @Override
    public Roles list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllRoles,
                                                  new MultilevelAdministrationsQueriesParameters()));
    }

    @Override
    public Response add(Role role) {
        validateParameters(role, "name", "permits.id");
        return performCreation(VdcActionType.AddRoleWithActionGroups,
                               new RoleWithActionGroupsParameters(map(role), mapPermits(role.getPermits().getPermits())),
                               new QueryIdResolver(VdcQueryType.GetRoleById, MultilevelAdministrationByRoleIdParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveRole, new RolesParameterBase(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public RoleResource getRoleSubResource(String id) {
        return inject(new BackendRoleResource(id));
    }

    protected Roles mapCollection(List<roles> entities) {
        Roles collection = new Roles();
        for (roles entity : entities) {
            collection.getRoles().add(addLinks(map(entity)));
        }
        return collection;
    }

    protected ArrayList<ActionGroup> mapPermits(List<Permit> permits) {
        ArrayList<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        Mapper<Permit, ActionGroup> mapper = getMapper(Permit.class, ActionGroup.class);
        for (Permit permit : permits) {
            actionGroups.add(mapper.map(permit, (ActionGroup)null));
        }
        return actionGroups;
    }

}
