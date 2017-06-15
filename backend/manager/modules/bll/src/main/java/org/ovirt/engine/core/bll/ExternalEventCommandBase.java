package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

public abstract class ExternalEventCommandBase<T extends ActionParametersBase> extends CommandBase<T> {

    public ExternalEventCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected List<PermissionSubject> getPermissionList(AuditLog event){
        List<PermissionSubject> permissionList = new ArrayList<>();
        if (event.getStorageDomainId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getStorageDomainId().toString()),
                VdcObjectType.Storage, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getStoragePoolId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getStoragePoolId().toString()),
                VdcObjectType.StoragePool, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getUserId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getUserId().toString()),
                VdcObjectType.User, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getClusterId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getClusterId().toString()),
                VdcObjectType.Cluster, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getVmId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getVmId().toString()),
                VdcObjectType.VM, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getVmTemplateId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getVmTemplateId().toString()),
                VdcObjectType.VmTemplate, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (event.getVdsId() != null) {
            permissionList.add(new PermissionSubject(new Guid(event.getVdsId().toString()),
                    VdcObjectType.VDS, ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        if (permissionList.isEmpty()) { // Global Event
            permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_EVENTS));
        }
        return permissionList;
    }
}
