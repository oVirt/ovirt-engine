package org.ovirt.engine.core.bll;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action runner runs the given commands in sequential order. Before executing the next command the validation and
 * the execution of the current command has to be completed. This ensures that the validation of the new command sees
 * the changes of the command which ran before.
 */
public class SequentialMultipleActionsRunner implements MultipleActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(SequentialMultipleActionsRunner.class);

    private final ActionType actionType;
    private final List<ActionParametersBase> parameters;
    private final CommandContext commandContext;
    private final boolean isInternal;
    private final ArrayList<ActionReturnValue> returnValues = new ArrayList<>();

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    NestedCommandFactory commandFactory;

    public SequentialMultipleActionsRunner(ActionType actionType,
            List<ActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        this.actionType = requireNonNull(actionType);
        this.parameters = requireNonNull(parameters);
        if (parameters.isEmpty()) {
            throw new IllegalArgumentException("Multiple actions runner received an empty parameter list.");
        }
        this.commandContext = commandContext;
        this.isInternal = isInternal;
    }

    @Override
    public ArrayList<ActionReturnValue> execute() {
        for (ActionParametersBase parameter : parameters) {
            if(!isInternal) {
                logExecution(log, sessionDataContainer, parameter.getSessionId(), String.format("command %s", actionType));
            }
            CommandBase<?> command =
                    commandFactory.createWrappedCommand(commandContext, actionType, parameter, isInternal);
            commandFactory.prepareCommandForMonitoring(commandContext, command);
            returnValues.add(command.executeAction());
        }
        return returnValues;
    }

    @Override
    public void setIsRunOnlyIfAllValidatePass(boolean isRunOnlyIfAllValidationPass) {
    }

    @Override
    public void setIsWaitForResult(boolean waitForResult) {
    }
}
