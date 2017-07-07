package org.ovirt.engine.core.bll.interfaces;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.DateTime;

public interface BackendInternal extends BackendLocal {

    ActionReturnValue runInternalAction(ActionType actionType, ActionParametersBase parameters);

    /**
     * Executes an action internally.
     *
     * @param actionType
     *            The type which define the action. Correlated to a concrete {@code CommandBase} instance.
     * @param parameters
     *            The parameters which are used to create the command.
     * @param context
     *            The context by which the command is being executed.
     * @return The result of executing the action
     */
    ActionReturnValue runInternalAction(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext context);

    /**
     * End the command with the passed compensation context, so that the calling command can compensate the internal
     * changes if need to.
     *
     * @param actionType
     *            The type of command to end.
     * @param parameters
     *            The command's parameters.
     * @param context
     *            The context by which the command should be ended
     * @return The result of the command ending.
     */
    ActionReturnValue endAction(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext context);

    QueryReturnValue runInternalQuery(QueryType actionType,
            QueryParametersBase parameters,
            EngineContext context);

    List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters);

    /**
     * Invokes multiple actions of the same action type with different parameters under a given command context which if
     * contains an execution context, determines the visibility of the action
     * determines the visibility of the action.<br/>
     * The context determines the monitoring of the action:
     * <ul>
     * <li>If {@code executionContext} is null, default implementation will create {@code Job} instance to monitor a
     * command for non-internal invocations.</li>
     * <li>If {@code executionContext} is configured for monitoring, a {@code Job} entity will be created for each
     * command which ends the validation successfully.</li>
     * </ul>
     *
     * @param actionType
     *            The type of the action
     * @param parameters
     *            A list containing the parameters for creating the command
     * @param commandContext
     *            Determines the visibility of the actions.
     * @return A collection of the results of each action validation.
     */
    List<ActionReturnValue> runInternalMultipleActions(ActionType actionType,
            List<ActionParametersBase> parameters, CommandContext commandContext);

    DateTime getStartedAt();

    QueryReturnValue runInternalQuery(QueryType queryType, QueryParametersBase queryParameters);

}
