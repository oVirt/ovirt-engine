package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AddExternalStepCommand <T extends AddExternalStepParameters> extends AddStepCommand<T>{


    public AddExternalStepCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue=super.validate();

        if (job != null) {
            if (!job.isExternal()) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NOT_EXTERNAL);
            }
        }

        if (!retValue) {
            addValidationMessage(EngineMessage.VAR__ACTION__ADD);
            addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_JOB);
        }
        return retValue;
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
