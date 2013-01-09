package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class MultipleActionsRunner {

    private static Log log = LogFactory.getLog(MultipleActionsRunner.class);
    private final static int CONCURRENT_ACTIONS = 10;

    private VdcActionType _actionType = VdcActionType.Unknown;
    private List<VdcActionParametersBase> _parameters;
    private final ArrayList<CommandBase<?>> _commands = new ArrayList<CommandBase<?>>();
    private final Map<Guid, Boolean> hasCorrelationIdMap = new HashMap<Guid, Boolean>();
    protected boolean isInternal;

    /**
     * Execute the actions only if CanDo of all the requests returns true
     */
    protected boolean isRunOnlyIfAllCanDoPass = false;

    /**
     * The context by which each command should be executed (monitored or non-monitored).
     */
    private ExecutionContext executionContext;

    public MultipleActionsRunner(VdcActionType actionType, List<VdcActionParametersBase> parameters, boolean isInternal) {
        _actionType = actionType;
        _parameters = parameters;
        this.isInternal = isInternal;
    }

    protected List<VdcActionParametersBase> getParameters() {
        return _parameters;
    }

    protected ArrayList<CommandBase<?>> getCommands() {
        return _commands;
    }

    public ArrayList<VdcReturnValueBase> Execute() {
        // sanity - don't do anything if no parameters passed
        if (_parameters == null || _parameters.isEmpty()) {
            log.infoFormat("{0} of type {1} invoked with no actions", this.getClass().getSimpleName(), _actionType);
            return new ArrayList<VdcReturnValueBase>();
        }

        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<VdcReturnValueBase>();
        try {
            VdcReturnValueBase returnValue;
            for (VdcActionParametersBase parameter : getParameters()) {
                parameter.setMultipleAction(true);
                boolean hasCorrelationId = StringUtils.isNotEmpty(parameter.getCorrelationId());
                returnValue = ExecutionHandler.evaluateCorrelationId(parameter);
                if (returnValue == null) {
                    CommandBase<?> command = CommandsFactory.CreateCommand(_actionType, parameter);
                    command.setInternalExecution(isInternal);
                    getCommands().add(command);
                    hasCorrelationIdMap.put(command.getCommandId(), hasCorrelationId);
                } else {
                    returnValues.add(returnValue);
                }
            }

            if (getCommands().size() == 1) {
                returnValues.add(getCommands().get(0).canDoActionOnly());
            } else {
                CheckCanDoActionsAsyncroniousely(returnValues);
            }

            boolean canRunActions = true;
            if (isRunOnlyIfAllCanDoPass) {
                for (VdcReturnValueBase value : returnValues) {
                    if (!value.getCanDoAction()) {
                        canRunActions = false;
                        break;
                    }
                }
            }

            if (canRunActions) {
                ThreadPoolUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        RunCommands();
                    }
                });
            }
        } catch (RuntimeException e) {
            log.error("Failed to execute multiple actions of type: " + _actionType, e);
        }
        return returnValues;
    }

    /**
     * Check CanDoActions of all commands. We perform checks for all commands at
     * the same time the number of threads is managed by java
     *
     * @param returnValues
     * @param executorService
     */
    private void CheckCanDoActionsAsyncroniousely(
                                                  ArrayList<VdcReturnValueBase> returnValues) {
        for (int i = 0; i < getCommands().size(); i += CONCURRENT_ACTIONS) {
            int handleSize = Math.min(CONCURRENT_ACTIONS, getCommands().size() - i);
            CountDownLatch latch = new CountDownLatch(handleSize);

            int fixedSize = i + handleSize;
            for (int j = i; j < fixedSize; j++) {
                RunCanDoActionAsyncroniousely(returnValues, j, fixedSize, latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
            }
        }
    }

    protected void runCanDoActionOnly(final ArrayList<VdcReturnValueBase> returnValues,
            final int currentCanDoActionId,
            final int totalSize,
            final CountDownLatch latch) {
        CommandBase<?> command = getCommands().get(currentCanDoActionId);
        if (command != null) {
            String actionType = command.getActionType().toString();
            try {
                log.infoFormat("Start time: {0}. Start running CanDoAction for command number {1}/{2} (Command type: {3})",
                        new Date(),
                        currentCanDoActionId + 1,
                        totalSize,
                        actionType);
                VdcReturnValueBase returnValue = command.canDoActionOnly();
                synchronized (returnValues) {
                    returnValues.add(returnValue);
                }
            } catch (RuntimeException e) {
                log.errorFormat("Failed to execute CanDoAction() for command number {0}/{1} (Command type: {2}), Error: {3}",
                        currentCanDoActionId + 1,
                        totalSize,
                        actionType,
                        e);
            } finally {
                latch.countDown();
                log.infoFormat("End time: {0}. Finish handling CanDoAction for command number {1}/{2} (Command type: {3})",
                        new Date(),
                        currentCanDoActionId + 1,
                        totalSize,
                        actionType);
            }
        }
        else {
            log.errorFormat("Failed to execute CanDoAction() for command number {0}/{1}. Command is null.",
                    currentCanDoActionId + 1,
                    totalSize);
        }
    }

    private void RunCanDoActionAsyncroniousely(
                                               final ArrayList<VdcReturnValueBase> returnValues,
                                               final int currentCanDoActionId, final int totalSize, final CountDownLatch latch) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                runCanDoActionOnly(returnValues, currentCanDoActionId, totalSize, latch);
            }
        });
    }

    protected void RunCommands() {
        for (CommandBase<?> command : getCommands()) {
            executeValidatedCommands(command);
        }
    }

    /**
     * Executes commands which passed validation and creates monitoring objects.
     *
     * @param command
     *            The command to execute
     */
    protected void executeValidatedCommands(CommandBase<?> command) {
        if (command.getReturnValue().getCanDoAction()) {

            if (executionContext == null || executionContext.isMonitored()) {
                ExecutionHandler.prepareCommandForMonitoring(command,
                        command.getActionType(),
                        command.isInternalExecution(),
                        new Boolean(hasCorrelationIdMap.get(command.getCommandId())));
            }
            ThreadLocalParamsContainer.setCorrelationId(command.getCorrelationId());
            command.executeAction();
        }
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setIsRunOnlyIfAllCanDoPass(boolean isRunOnlyIfAllCanDoPass) {
        this.isRunOnlyIfAllCanDoPass = isRunOnlyIfAllCanDoPass;
    }

}
