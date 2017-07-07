package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrevalidatingMultipleActionsRunner implements MultipleActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(PrevalidatingMultipleActionsRunner.class);
    private static final int CONCURRENT_ACTIONS = 10;

    private ActionType actionType = ActionType.Unknown;
    private final Set<ActionParametersBase> parameters;
    private final List<CommandBase<?>> commands = new ArrayList<>();
    protected boolean isInternal;
    private boolean isWaitForResult = false;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    NestedCommandFactory commandFactory;

    /**
     * Execute the actions only if Validate of all the requests returns true
     */
    protected boolean isRunOnlyIfAllValidationPass = false;

    protected CommandContext commandContext;

    public PrevalidatingMultipleActionsRunner(ActionType actionType,
            List<ActionParametersBase> parameters,
            CommandContext commandContext,
            boolean isInternal) {
        this.actionType = actionType;
        this.isInternal = isInternal;
        this.commandContext = commandContext;
        this.parameters = new LinkedHashSet<>(parameters);
    }

    protected Set<ActionParametersBase> getParameters() {
        return parameters;
    }

    protected List<CommandBase<?>> getCommands() {
        return commands;
    }

    @Override
    public List<ActionReturnValue> execute() {
        // sanity - don't do anything if no parameters passed
        if (parameters == null || parameters.isEmpty()) {
            log.info("{} of type '{}' invoked with no actions", this.getClass().getSimpleName(), actionType);
            return new ArrayList<>();
        }

        List<ActionReturnValue> returnValues = new ArrayList<>();
        try {
            initCommandsAndReturnValues(returnValues);

            invokeCommands(returnValues);
        } catch (RuntimeException e) {
            log.error("Failed to execute multiple actions of type '{}': {}", actionType, e.getMessage());
            log.error("Exception", e);
        }
        return returnValues;
    }

    private void initCommandsAndReturnValues(List<ActionReturnValue> returnValues) {
        ActionReturnValue returnValue;
        for (ActionParametersBase parameter : getParameters()) {
            parameter.setMultipleAction(true);
            returnValue = ExecutionHandler.evaluateCorrelationId(parameter);
            if (returnValue == null) {
                getCommands()
                        .add(commandFactory.createWrappedCommand(commandContext, actionType, parameter, isInternal));
            } else {
                returnValues.add(returnValue);
            }
        }
    }

    private void invokeCommands(List<ActionReturnValue> returnValues) {
        if (canRunActions(returnValues)) {
            if (isWaitForResult) {
                invokeSyncCommands();
            } else {
                invokeCommands();
            }
        } else if (isRunOnlyIfAllValidationPass) {
            freeLockForValidationPassedCommands();
        }
    }

    private boolean canRunActions(List<ActionReturnValue> returnValues) {
        if (getCommands().size() == 1) {
            CorrelationIdTracker.setCorrelationId(getCommands().get(0).getCorrelationId());
            returnValues.add(getCommands().get(0).validateOnly());
        } else {
            checkValidatesAsynchronously(returnValues);
        }

        if (isRunOnlyIfAllValidationPass) {
            for (ActionReturnValue value : returnValues) {
                if (!value.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void freeLockForValidationPassedCommands() {
        getCommands().stream().filter(command->command.getReturnValue().isValid()).forEach(command->command.freeLock());
    }

    /**
     * Check Validates of all commands. We perform checks for all commands at
     * the same time the number of threads is managed by java
     */
    private void checkValidatesAsynchronously(
            List<ActionReturnValue> returnValues) {
        for (int i = 0; i < getCommands().size(); i += CONCURRENT_ACTIONS) {
            int handleSize = Math.min(CONCURRENT_ACTIONS, getCommands().size() - i);

            int fixedSize = i + handleSize;
            List<Callable<ActionReturnValue>> validateTasks = new ArrayList<>();
            for (int j = i; j < fixedSize; j++) {
                validateTasks.add(buildValidateAsynchronously(j, fixedSize));
            }
            returnValues.addAll(ThreadPoolUtil.invokeAll(validateTasks));
        }
    }

    private Callable<ActionReturnValue> buildValidateAsynchronously(
            final int currentValidateId, final int totalSize) {
        return () -> runValidateOnly(currentValidateId, totalSize);
    }

    protected ActionReturnValue runValidateOnly(final int currentValidateId, final int totalSize) {
        CommandBase<?> command = getCommands().get(currentValidateId);
        String actionType = command.getActionType().toString();
        CorrelationIdTracker.setCorrelationId(command.getCorrelationId());
        try {
            log.info("Start running Validate for command number {}/{} (Command type '{}')",
                    currentValidateId + 1,
                    totalSize,
                    actionType);
            return command.validateOnly();
        } finally {
            log.info("Finish handling Validate for command number {}/{} (Command type '{}')",
                    currentValidateId + 1,
                    totalSize,
                    actionType);
        }
    }

    protected void runCommands() {
        for (CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().isValid()) {
                executeValidatedCommand(command);
            }
        }
    }

    protected void invokeCommands() {
        ThreadPoolUtil.execute(() -> runCommands());
    }

    protected void invokeSyncCommands() {
        runCommands();
    }

    /**
     * Executes commands which passed validation and creates monitoring objects.
     *
     * @param command
     *            The command to execute
     */

    protected void executeValidatedCommand(CommandBase<?> command) {
        if (!isInternal) {
            logExecution(log,
                    sessionDataContainer,
                    command.getParameters().getSessionId(),
                    String.format("command %s", actionType));
        }
        commandFactory.prepareCommandForMonitoring(commandContext, command);
        command.executeAction();
    }

    @Override
    public void setIsRunOnlyIfAllValidatePass(boolean isRunOnlyIfAllValidationPass) {
        this.isRunOnlyIfAllValidationPass = isRunOnlyIfAllValidationPass;
    }

    @Override
    public void setIsWaitForResult(boolean waitForResult) {
        this.isWaitForResult = waitForResult;
    }
}
