package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(MultipleActionsRunner.class);
    private final static int CONCURRENT_ACTIONS = 10;

    private VdcActionType actionType = VdcActionType.Unknown;
    private final Set<VdcActionParametersBase> parameters;
    private final ArrayList<CommandBase<?>> commands = new ArrayList<CommandBase<?>>();
    protected boolean isInternal;
    private boolean isWaitForResult = false;

    /**
     * Execute the actions only if CanDo of all the requests returns true
     */
    protected boolean isRunOnlyIfAllCanDoPass = false;

    protected CommandContext commandContext;

    public MultipleActionsRunner(VdcActionType actionType, List<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
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

    public ArrayList<VdcReturnValueBase> execute() {
        // sanity - don't do anything if no parameters passed
        if (parameters == null || parameters.isEmpty()) {
            log.info("{} of type '{}' invoked with no actions", this.getClass().getSimpleName(), actionType);
            return new ArrayList<VdcReturnValueBase>();
        }

        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<VdcReturnValueBase>();
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
                CommandBase<?> command = isInternal ?
                        CommandsFactory.createCommand(actionType, parameter, commandContext.clone()
                                .withoutCompensationContext()) :
                                CommandsFactory.createCommand(actionType, parameter);

                command.setInternalExecution(isInternal);
                getCommands().add(command);
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
            returnValues.add(getCommands().get(0).canDoActionOnly());
        } else {
            checkCanDoActionsAsynchronously(returnValues);
        }

        if (isRunOnlyIfAllCanDoPass) {
            for (VdcReturnValueBase value : returnValues) {
                if (!value.getCanDoAction()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check CanDoActions of all commands. We perform checks for all commands at
     * the same time the number of threads is managed by java
     *
     * @param returnValues
     */
    private void checkCanDoActionsAsynchronously(
            ArrayList<VdcReturnValueBase> returnValues) {
        for (int i = 0; i < getCommands().size(); i += CONCURRENT_ACTIONS) {
            int handleSize = Math.min(CONCURRENT_ACTIONS, getCommands().size() - i);

            int fixedSize = i + handleSize;
            List<Callable<VdcReturnValueBase>> canDoActionTasks = new ArrayList<Callable<VdcReturnValueBase>>();
            for (int j = i; j < fixedSize; j++) {
                canDoActionTasks.add(buildCanDoActionAsynchronously(j, fixedSize));
            }
            returnValues.addAll(ThreadPoolUtil.invokeAll(canDoActionTasks));
        }
    }

    private Callable<VdcReturnValueBase> buildCanDoActionAsynchronously(
            final int currentCanDoActionId, final int totalSize) {
        return new Callable<VdcReturnValueBase>() {

            @Override
            public VdcReturnValueBase call() {
                return runCanDoActionOnly(currentCanDoActionId, totalSize);
            }
        };
    }

    protected VdcReturnValueBase runCanDoActionOnly(final int currentCanDoActionId, final int totalSize) {
        CommandBase<?> command = getCommands().get(currentCanDoActionId);
        String actionType = command.getActionType().toString();
        CorrelationIdTracker.setCorrelationId(command.getCorrelationId());
        try {
            log.info("Start running CanDoAction for command number {}/{} (Command type '{}')",
                    currentCanDoActionId + 1,
                    totalSize,
                    actionType);
            return command.canDoActionOnly();
        } finally {
            log.info("Finish handling CanDoAction for command number {}/{} (Command type '{}')",
                    currentCanDoActionId + 1,
                    totalSize,
                    actionType);
        }
    }

    protected void runCommands() {
        for (CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().getCanDoAction()) {
                executeValidatedCommand(command);
            }
        }
    }

    protected void invokeCommands() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                runCommands();
            }
        });
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
        if (commandContext == null || commandContext.getExecutionContext() == null || commandContext.getExecutionContext().isMonitored()) {
            ExecutionHandler.prepareCommandForMonitoring(command,
                    command.getActionType(),
                    command.isInternalExecution());
        }
        CorrelationIdTracker.setCorrelationId(command.getCorrelationId());
        command.executeAction();
    }

    public void setIsRunOnlyIfAllCanDoPass(boolean isRunOnlyIfAllCanDoPass) {
        this.isRunOnlyIfAllCanDoPass = isRunOnlyIfAllCanDoPass;
    }

    public void setIsWaitForResult(boolean waitForResult) {
        this.isWaitForResult = waitForResult;
    }
}
