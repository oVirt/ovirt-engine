package org.ovirt.engine.core.bll.interfaces;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.DateTime;

public interface BackendInternal extends BackendLocal {

    VdcReturnValueBase runInternalAction(VdcActionType actionType, VdcActionParametersBase parameters);

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
    VdcReturnValueBase runInternalAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
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
    VdcReturnValueBase endAction(VdcActionType actionType,
            VdcActionParametersBase parameters,
            CommandContext context);

    VdcQueryReturnValue runInternalQuery(VdcQueryType actionType,
            VdcQueryParametersBase parameters,
            EngineContext context);

    ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters);

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
    ArrayList<VdcReturnValueBase> runInternalMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext);

    DateTime getStartedAt();

    VdcQueryReturnValue runInternalQuery(VdcQueryType queryType, VdcQueryParametersBase queryParameters);

}
