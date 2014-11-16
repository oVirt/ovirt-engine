package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
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
        // VM_BASIC_OPERATIONS is deprecated in ActionGroup
        // We are building Permit of VM_BASIC_OPERATIONS for backward compatibility,
        // We are using RUN_VM since its one of VM_BASIC_OPERATIONS
        if (id.equals(PermitType.getVmBasicOperationsId())) {
            Permit p = new Permit();
            p.setName(PermitType.VM_BASIC_OPERATIONS.toString().toLowerCase());
            p.setId(PermitType.getVmBasicOperationsId());
            ActionGroup runVm = parent.lookupId(String.valueOf(ActionGroup.RUN_VM.getId()));
            p.setAdministrative(org.ovirt.engine.api.model.RoleType.ADMIN.toString().equals(runVm.getRoleType().toString()));
            return addLinks(p);
        }
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
