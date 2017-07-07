package org.ovirt.engine.core.bll.interfaces;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;

/**
 * Interface for direct operations on {@link CommandBase} objects The reason for introducing this interface is that it
 * is desired that in case a command should be created it should be still done by {@link CommandsFactory} at the
 * {@link Backend} bean code.
 */
public interface BackendCommandObjectsHandler {

    /**
     * Creates an instance of the action. This should be used by {@code CommandBase} to insert place holders for tasks
     *
     * @param actionType
     *            the type of the command to run
     * @param parameters
     *            parameters of the command
     * @return object of the created command
     */
    CommandBase<?> createAction(ActionType actionType, ActionParametersBase parameters, CommandContext context);

    /**
     * Executes the instance of the action. This should be used by parent/root commands in order to execute child
     * commands that place holders were created for them.
     */
    ActionReturnValue runAction(CommandBase<?> action, ExecutionContext executionContext);

}
