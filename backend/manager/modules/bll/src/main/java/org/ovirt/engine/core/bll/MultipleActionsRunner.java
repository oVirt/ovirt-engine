package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class MultipleActionsRunner {

    private final int CONCURRENT_ACTIONS = 10;

    private VdcActionType _actionType = VdcActionType.forValue(0);
    private List<VdcActionParametersBase> _parameters;
    private final java.util.ArrayList<CommandBase> _commands = new java.util.ArrayList<CommandBase>();
    protected boolean isInternal;

    public MultipleActionsRunner(VdcActionType actionType, List<VdcActionParametersBase> parameters, boolean isInternal) {
        _actionType = actionType;
        _parameters = parameters;
        this.isInternal = isInternal;
    }

    protected List<VdcActionParametersBase> getParameters() {
        return _parameters;
    }

    protected java.util.ArrayList<CommandBase> getCommands() {
        return _commands;
    }

    public java.util.ArrayList<VdcReturnValueBase> Execute() {
        // sanity - don't do anything if no parameters passed
        if (_parameters == null || _parameters.isEmpty()) {
            log.infoFormat("{0} of type {1} invoked with no actions", this.getClass().getSimpleName(), _actionType);
            return new ArrayList<VdcReturnValueBase>();
        }

        java.util.ArrayList<VdcReturnValueBase> returnValues = new java.util.ArrayList<VdcReturnValueBase>();
        try {

            for (VdcActionParametersBase parameter : getParameters()) {
                parameter.setMultipleAction(true);
                CommandBase command = CommandsFactory.CreateCommand(_actionType, parameter);
                command.setInternalExecution(isInternal);
                getCommands().add(command);
            }
            if (getCommands().size() == 1) {
                returnValues.add(getCommands().get(0).CanDoActionOnly());
            } else {
                CheckCanDoActionsAsyncroniousely(returnValues);
            }
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    RunCommands();
                }
            });
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
                                                  java.util.ArrayList<VdcReturnValueBase> returnValues) {
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

    private void RunCanDoActionAsyncroniousely(
                                               final java.util.ArrayList<VdcReturnValueBase> returnValues,
                                               final int currentCanDoActionId, final int totalSize, final CountDownLatch latch) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                CommandBase command = getCommands().get(currentCanDoActionId);
                if (command != null) {
                    String actionType = command.getActionType().toString();
                    try {
                        log.infoFormat("Start time: {0}. Start running CanDoAction for command number {1}/{2} (Command type: {3})",
                                new java.util.Date(),
                                currentCanDoActionId + 1,
                                totalSize,
                                actionType);
                        VdcReturnValueBase returnValue = command.CanDoActionOnly();
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
                                new java.util.Date(),
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
        });
    }

    protected void RunCommands() {
        for (CommandBase command : getCommands()) {
            if (command.getReturnValue().getCanDoAction()) {
                command.ExecuteAction();
            }
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(MultipleActionsRunner.class);
}
