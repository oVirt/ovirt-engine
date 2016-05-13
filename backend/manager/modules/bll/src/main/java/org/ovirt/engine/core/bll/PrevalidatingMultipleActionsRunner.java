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
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrevalidatingMultipleActionsRunner implements MultipleActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(PrevalidatingMultipleActionsRunner.class);
    private static final int CONCURRENT_ACTIONS = 10;

    private VdcActionType actionType = VdcActionType.Unknown;
    private final Set<VdcActionParametersBase> parameters;
    private final ArrayList<CommandBase<?>> commands = new ArrayList<>();
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

    public PrevalidatingMultipleActionsRunner(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            CommandContext commandContext,
            boolean isInternal) {
        this.actionType = actionType;
        this.isInternal = isInternal;
        this.commandContext = commandContext;
        this.parameters = new LinkedHashSet<>(parameters);
    }

    protected Set<VdcActionParametersBase> getParameters() {
        return parameters;
    }

    protected ArrayList<CommandBase<?>> getCommands() {
        return commands;
    }

    @Override
    public ArrayList<VdcReturnValueBase> execute() {
        // sanity - don't do anything if no parameters passed
        if (parameters == null || parameters.isEmpty()) {
            log.info("{} of type '{}' invoked with no actions", this.getClass().getSimpleName(), actionType);
            return new ArrayList<>();
        }

        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<>();
        try {
            initCommandsAndReturnValues(returnValues);

            invokeCommands(returnValues);
        } catch (RuntimeException e) {
            log.error("Failed to execute multiple actions of type '{}': {}", actionType, e.getMessage());
            log.error("Exception", e);
        }
        return returnValues;
    }

    private void initCommandsAndReturnValues(ArrayList<VdcReturnValueBase> returnValues) {
        VdcReturnValueBase returnValue;
        for (VdcActionParametersBase parameter : getParameters()) {
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

    private void invokeCommands(ArrayList<VdcReturnValueBase> returnValues) {
        if (canRunActions(returnValues)) {
            if (isWaitForResult) {
                invokeSyncCommands();
            } else {
                invokeCommands();
            }
        }
    }

    private boolean canRunActions(ArrayList<VdcReturnValueBase> returnValues) {
        if (getCommands().size() == 1) {
            CorrelationIdTracker.setCorrelationId(getCommands().get(0).getCorrelationId());
            returnValues.add(getCommands().get(0).validateOnly());
        } else {
            checkValidatesAsynchronously(returnValues);
        }

        if (isRunOnlyIfAllValidationPass) {
            for (VdcReturnValueBase value : returnValues) {
                if (!value.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check Validates of all commands. We perform checks for all commands at
     * the same time the number of threads is managed by java
     */
    private void checkValidatesAsynchronously(
            ArrayList<VdcReturnValueBase> returnValues) {
        for (int i = 0; i < getCommands().size(); i += CONCURRENT_ACTIONS) {
            int handleSize = Math.min(CONCURRENT_ACTIONS, getCommands().size() - i);

            int fixedSize = i + handleSize;
            List<Callable<VdcReturnValueBase>> validateTasks = new ArrayList<>();
            for (int j = i; j < fixedSize; j++) {
                validateTasks.add(buildValidateAsynchronously(j, fixedSize));
            }
            returnValues.addAll(ThreadPoolUtil.invokeAll(validateTasks));
        }
    }

    private Callable<VdcReturnValueBase> buildValidateAsynchronously(
            final int currentValidateId, final int totalSize) {
        return () -> runValidateOnly(currentValidateId, totalSize);
    }

    protected VdcReturnValueBase runValidateOnly(final int currentValidateId, final int totalSize) {
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
