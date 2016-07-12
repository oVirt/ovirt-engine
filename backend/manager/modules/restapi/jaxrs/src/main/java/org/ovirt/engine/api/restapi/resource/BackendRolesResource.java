package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendRolesResource
        extends AbstractBackendCollectionResource<Role, org.ovirt.engine.core.common.businessentities.Role>
        implements RolesResource {

    static final String[] SUB_COLLECTIONS = { "permits" };

    public BackendRolesResource() {
        super(Role.class, org.ovirt.engine.core.common.businessentities.Role.class, SUB_COLLECTIONS);
    }

    @Override
    public Roles list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllRoles,
                                                  new MultilevelAdministrationsQueriesParameters()));
    }

    @Override
    public Response add(Role role) {
        validateParameters(role, "name", "permits.id");
        validatePermitId(role);
        return performCreate(VdcActionType.AddRoleWithActionGroups,
                               new RoleWithActionGroupsParameters(map(role), mapPermits(role.getPermits().getPermits())),
                               new QueryIdResolver<Guid>(VdcQueryType.GetRoleById, IdQueryParameters.class));
    }

    private void validatePermitId(Role role) {
        for (Permit permit : role.getPermits().getPermits()) {
            if (permit.isSetId()) {
                ActionGroup actionGroup = ActionGroup.forValue(Integer.parseInt(permit.getId()));
                if (actionGroup == null) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(permit.getId() + " is not a valid permit ID.")
                            .build());
                }
            }
        }
    }

    @Override
    public RoleResource getRoleResource(String id) {
        return inject(new BackendRoleResource(id));
    }

    protected Roles mapCollection(List<org.ovirt.engine.core.common.businessentities.Role> entities) {
        Roles collection = new Roles();
        for (org.ovirt.engine.core.common.businessentities.Role entity : entities) {
            collection.getRoles().add(addLinks(map(entity)));
        }
        return collection;
    }

    protected ArrayList<ActionGroup> mapPermits(List<Permit> permits) {
        ArrayList<ActionGroup> actionGroups = new ArrayList<>();
        Mapper<Permit, ActionGroup> mapper = getMapper(Permit.class, ActionGroup.class);
        actionGroups.addAll(permits.stream().map(p -> mapper.map(p, (ActionGroup) null)).collect(Collectors.toList()));
        return actionGroups;
    }
}
