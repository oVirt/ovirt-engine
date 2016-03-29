package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunAsyncActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

/**
 * Execute the given action using the parameters using CoCo
 */
public class RunAsyncActionCommand<T extends RunAsyncActionParameters> extends CommandBase<T> {

    public RunAsyncActionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        VdcActionType actionToExecute = getParameters().getAction();
        VdcActionParametersBase actionParameters = getParameters().getActionParameters();
        actionParameters.setParentCommand(VdcActionType.RunAsyncAction);
        actionParameters.setParentParameters(getParameters());
        CommandCoordinatorUtil.executeAsyncCommand(
                actionToExecute,
                actionParameters,
                cloneContextAndDetachFromParent());
        setSucceeded(true);
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }
}
