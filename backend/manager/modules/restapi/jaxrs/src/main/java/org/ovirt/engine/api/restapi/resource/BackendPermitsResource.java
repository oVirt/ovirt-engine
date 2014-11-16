package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
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
                                                  new IdQueryParameters(roleId)));
    }

    @Override
    public Response add(Permit permit) {
        validateParameters(permit, "name|id");
        validateEnums(Permit.class, permit);
        ArrayList<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        // VM_BASIC_OPERATIONS is deprecated, its now more detailed with the following:
        // REBOOT_VM, STOP_VM, SHUT_DOWN_VM, HIBERNATE_VM and RUN_VM
        // We use addIfNotExists since user may send VM_BASIC_OPERATIONS with RUN_VM etc.
        if (PermitType.getVmBasicOperationsId().equals(permit.getId()) ||
            permit.getName() != null && permit.getName().toLowerCase().equals(PermitType.VM_BASIC_OPERATIONS.toString().toLowerCase())) {
            addIfNotExists(actionGroups, ActionGroup.REBOOT_VM);
            addIfNotExists(actionGroups, ActionGroup.STOP_VM);
            addIfNotExists(actionGroups, ActionGroup.SHUT_DOWN_VM);
            addIfNotExists(actionGroups, ActionGroup.HIBERNATE_VM);
            addIfNotExists(actionGroups, ActionGroup.RUN_VM);
        } else {
            addIfNotExists(actionGroups, map(permit));
        }
        return performCreate(VdcActionType.AttachActionGroupsToRole,
                               new ActionGroupsToRoleParameter(roleId, actionGroups),
                               new PermitIdResolver(actionGroups.get(0)));
    }

    private void addIfNotExists(List<ActionGroup> list, ActionGroup item) {
        if (!list.contains(item)) {
            list.add(item);
        }
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
        // VM_BASIC_OPERATIONS is deprecated, its now more detailed with the following:
        // REBOOT_VM, STOP_VM, SHUT_DOWN_VM, HIBERNATE_VM and RUN_VM
        // for backward compatibility we show the user VM_BASIC_OPERATIONS if all this
        // ActionGroup are present
        if (entities.contains(ActionGroup.REBOOT_VM) &&
            entities.contains(ActionGroup.STOP_VM) &&
            entities.contains(ActionGroup.SHUT_DOWN_VM) &&
            entities.contains(ActionGroup.HIBERNATE_VM) &&
            entities.contains(ActionGroup.RUN_VM)) {
            Permit p = new Permit();
            p.setName(PermitType.VM_BASIC_OPERATIONS.toString().toLowerCase());
            p.setId(PermitType.getVmBasicOperationsId());
            ActionGroup runVm = entities.get(entities.indexOf(ActionGroup.RUN_VM));
            p.setAdministrative(org.ovirt.engine.api.model.RoleType.ADMIN.toString().equals(runVm.getRoleType().toString()));
            collection.getPermits().add(addLinks(p));
        }
        for (ActionGroup entity : entities) {
            Permit permit = map(entity);
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

    @Override
    protected Permit doPopulate(Permit model, ActionGroup entity) {
        return model;
    }

}
