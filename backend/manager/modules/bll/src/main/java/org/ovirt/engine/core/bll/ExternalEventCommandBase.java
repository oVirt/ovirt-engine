package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

public abstract class ExternalEventCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {

    private static final long serialVersionUID = 1L;

    public ExternalEventCommandBase() {
        super();
    }

    public ExternalEventCommandBase(T parameters) {
        super(parameters);
    }

    public ExternalEventCommandBase(Guid commandId) {
        super(commandId);
    }

    protected List<PermissionSubject> getPermissionList(AuditLog event){
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        if (event.getstorage_domain_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getstorage_domain_id().toString()),
                VdcObjectType.Storage, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getstorage_pool_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getstorage_pool_id().toString()),
                VdcObjectType.StoragePool, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getuser_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getuser_id().toString()),
                VdcObjectType.User, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getvds_group_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getvds_group_id().toString()),
                VdcObjectType.VdsGroups, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getvm_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getvm_id().toString()),
                VdcObjectType.VM, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getvm_template_id() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getvm_template_id().toString()),
                VdcObjectType.VmTemplate, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (permissionList.isEmpty()) { // Global Event
            permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        return permissionList;
    }
}
