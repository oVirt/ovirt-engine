package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunAsyncActionParameters;

/**
 * Execute the given action using the parameters using CoCo
 */
public class RunAsyncActionCommand<T extends RunAsyncActionParameters> extends CommandBase<T> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private CommandBase<?> command;

    public RunAsyncActionCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        commandCoordinatorUtil.executeAsyncCommand(
                getParameters().getAction(),
                getParameters().getActionParameters(),
                cloneContextAndDetachFromParent());
        setSucceeded(true);
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return getCommand().isUserAuthorizedToRunAction();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return getCommand().getPermissionCheckSubjects();
    }

    private CommandBase<?> getCommand() {
        if (command == null) {
            command = CommandsFactory.createCommand(getParameters().getAction(),
                    getParameters().getActionParameters(),
                    cloneContextAndDetachFromParent());
        }
        return command;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
