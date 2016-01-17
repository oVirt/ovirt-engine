package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddInternalJobParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 *
 * BLL command to create a Job for a task that's already running. This is used so that tasks
 * that are started externally can be monitored from engine.
 */
@InternalCommandAttribute
public class AddInternalJobCommand<T extends AddInternalJobParameters> extends AddJobCommand<T> {

    public AddInternalJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {

        boolean retValue = super.validate();
        if (getParameters().getActionType() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_EMPTY_ACTION_TYPE);
            retValue = false;
        }
        return retValue;
    }


    @Override
    protected void executeCommand() {
        createJob(getParameters().getActionType(), false);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject>  permissionList = new ArrayList<>();
        if (getParameters().getJobEntityType() != null && getParameters().getJobEntityId() != null) {
            permissionList.add(new PermissionSubject(getParameters().getJobEntityId(),
                    getParameters().getJobEntityType(),
                    getParameters().getActionType().getActionGroup()));
        } else {
            permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getParameters().getActionType().getActionGroup()));
        }
        return permissionList;
    }

  }
