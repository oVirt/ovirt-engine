package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;


public class AddExternalJobCommand<T extends AddExternalJobParameters> extends AddJobCommand<T> {

    public AddExternalJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        createJob(ActionType.AddExternalJob, true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_EXTERNAL_JOB : AuditLogType.USER_ADD_EXTERNAL_JOB_FAILED;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject>  permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_TASKS));
        return permissionList;
    }
}
