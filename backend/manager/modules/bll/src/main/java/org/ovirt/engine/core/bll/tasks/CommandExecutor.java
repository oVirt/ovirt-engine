package org.ovirt.engine.core.bll.tasks;

import static org.ovirt.engine.core.bll.tasks.CommandsRepository.CommandContainer;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.BackendUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Config.<Integer>getValue(ConfigValues.CommandCoordinatorThreadPoolSize));
    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
    private final CommandsRepository commandsRepository;
    private boolean cmdExecutorInitialized;
    private final int pollingRate = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds);

    CommandExecutor(CommandsRepository commandsRepository) {
        this.commandsRepository = commandsRepository;
        SchedulerUtil scheduler = Injector.get(SchedulerUtilQuartzImpl.class);
        scheduler.scheduleAFixedDelayJob(this, "invokeCallbackMethods", new Class[]{},
                new Object[]{}, pollingRate, pollingRate, TimeUnit.SECONDS);
    }

    @OnTimerMethodAnnotation("invokeCallbackMethods")
    public void invokeCallbackMethods() {
        initCommandExecutor();
        Iterator<Entry<Guid, CommandContainer>> iterator = commandsRepository.getCommandsCallback().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Guid, CommandContainer> entry = iterator.next();

            // Decrement counter; execute if it reaches 0
            CommandContainer commandContainer = entry.getValue();
            commandContainer.setRemainingDelay(commandContainer.getRemainingDelay() - pollingRate);
            if (commandContainer.getRemainingDelay() > 0) {
                continue;
            }

            Guid cmdId = entry.getKey();
            CommandCallback callback = commandContainer.getCallback();
            CommandStatus status = commandsRepository.getCommandStatus(cmdId);
            boolean errorInCallback = false;
            try {
                switch (status) {
                    case FAILED:
                        callback.onFailed(cmdId, commandsRepository.getChildCommandIds(cmdId));
                        break;
                    case SUCCEEDED:
                        callback.onSucceeded(cmdId, commandsRepository.getChildCommandIds(cmdId));
                        break;
                    case ACTIVE:
                        if (commandsRepository.getCommandEntity(cmdId).isExecuted()) {
                            callback.doPolling(cmdId, commandsRepository.getChildCommandIds(cmdId));
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                errorInCallback = true;
                handleError(ex, status, cmdId);
            } finally {
                if (CommandStatus.FAILED.equals(status) || (CommandStatus.SUCCEEDED.equals(status) && !errorInCallback)) {
                    commandsRepository.updateCallbackNotified(cmdId);
                    iterator.remove();
                    CommandEntity cmdEntity = commandsRepository.getCommandEntity(entry.getKey());
                    if (cmdEntity != null) {
                        // When a child finishes, its parent's callback should execute shortly thereafter
                        Guid rootCommandId = cmdEntity.getRootCommandId();
                        if (!Guid.isNullOrEmpty(rootCommandId) && commandsRepository.getCommandsCallback().containsKey(rootCommandId)) {
                            commandsRepository.getCommandsCallback().get(rootCommandId).setInitialDelay(pollingRate);
                            commandsRepository.getCommandsCallback().get(rootCommandId).setRemainingDelay(pollingRate);
                        }
                    }
                } else if (status != commandsRepository.getCommandStatus(cmdId)) {
                    commandContainer.setInitialDelay(pollingRate);
                    commandContainer.setRemainingDelay(pollingRate);
                } else {
                    int maxDelay = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds);
                    commandContainer.setInitialDelay(Math.min(maxDelay, commandContainer.getInitialDelay() * 2));
                    commandContainer.setRemainingDelay(commandContainer.getInitialDelay());
                }
            }
        }
        if (!commandsRepository.getCommandsCallback().isEmpty()) {
            markExpiredCommandsAsFailure();
        }
    }

    private void markExpiredCommandsAsFailure() {
        for (Entry<Guid, CommandContainer> entry : commandsRepository.getCommandsCallback().entrySet()) {
            List<Guid> childCmdIds = commandsRepository.getChildCommandIds(entry.getKey());
            if (childCmdIds.isEmpty()) {
                markExpiredCommandAsFailure(entry.getKey());
            } else {
                childCmdIds.forEach(this::markExpiredCommandAsFailure);
            }
        }
    }

    private void markExpiredCommandAsFailure(Guid cmdId) {
        CommandEntity cmdEntity = commandsRepository.getCommandEntity(cmdId);
        if (cmdEntity != null && cmdEntity.getCommandStatus() == CommandStatus.ACTIVE) {
            Calendar cal = Calendar.getInstance();
            Integer cmdLifeTimeInMin = cmdEntity.getCommandParameters().getLifeInMinutes();
            cal.add(Calendar.MINUTE, -1 * (cmdLifeTimeInMin == null ?
                    Config.<Integer>getValue(ConfigValues.CoCoLifeInMinutes) :
                    cmdLifeTimeInMin));
            if (cmdEntity.getCreatedAt().getTime() < cal.getTime().getTime()) {
                log.warn("Marking expired command as Failed: command '{} ({})' that started at '{}' has been marked as Failed.",
                        cmdEntity.getCommandType(),
                        cmdEntity.getId(),
                        cmdEntity.getCreatedAt());
                commandsRepository.updateCommandStatus(cmdId, CommandStatus.FAILED);
            }
        }
    }

    private void handleError(Exception ex, CommandStatus status, Guid cmdId) {
        log.error("Error invoking callback method '{}' for '{}' command '{}'",
                getCallbackMethod(status),
                status,
                cmdId);
        log.error("Exception", ex);
        if (!CommandStatus.FAILED.equals(status)) {
            commandsRepository.updateCommandStatus(cmdId, CommandStatus.FAILED);
        }
    }

    private String getCallbackMethod(CommandStatus status) {
        switch (status) {
            case FAILED:
            case EXECUTION_FAILED:
                return "onFailed";
            case SUCCEEDED:
                return "onSucceeded";
            case ACTIVE:
                return "doPolling";
            default:
                return "Unknown";
        }
    }

    private void initCommandExecutor() {
        if (!cmdExecutorInitialized) {
            for (CommandEntity cmdEntity : commandsRepository.getCommands(true)) {
                if (!cmdEntity.isExecuted() &&
                        cmdEntity.getCommandStatus() != CommandStatus.FAILED &&
                        cmdEntity.getCommandStatus() != CommandStatus.EXECUTION_FAILED) {
                    commandsRepository.retrieveCommand(cmdEntity.getId()).setCommandStatus(CommandStatus.EXECUTION_FAILED);
                }
                if (!cmdEntity.isCallbackNotified()) {
                    commandsRepository.addToCallbackMap(cmdEntity);
                }
            }
            cmdExecutorInitialized = true;
        }
    }

    public Future<VdcReturnValueBase> executeAsyncCommand(final VdcActionType actionType,
                                                          final VdcActionParametersBase parameters,
                                                          final CommandContext cmdContext) {
        final CommandBase<?> command = CommandsFactory.createCommand(actionType, parameters, cmdContext);
        CommandCallback callBack = command.getCallback();
        command.persistCommand(command.getParameters().getParentCommand(), cmdContext, callBack != null);
        if (callBack != null) {
            commandsRepository.getCommandsCallback().put(command.getCommandId(), new CommandContainer(callBack, pollingRate));
        }

        Future<VdcReturnValueBase> retVal;
        try {
            retVal = executor.submit(() -> executeCommand(command, cmdContext));
        } catch (RejectedExecutionException ex) {
            command.setCommandStatus(CommandStatus.FAILED);
            log.error("Failed to submit command to executor service, command '{}' status has been set to FAILED",
                    command.getCommandId());
            retVal = new RejectedExecutionFuture();
        }
        return retVal;
    }

    private VdcReturnValueBase executeCommand(final CommandBase<?> command, final CommandContext cmdContext) {
        VdcReturnValueBase result = BackendUtils.getBackendCommandObjectsHandler(log).runAction(command,
                cmdContext != null ? cmdContext.getExecutionContext() : null);
        updateCommand(command, result);
        return result;
    }

    private void updateCommand(final CommandBase<?> command,
                               final VdcReturnValueBase result) {
        CommandEntity cmdEntity = commandsRepository.getCommandEntity(command.getCommandId());
        cmdEntity.setReturnValue(result);
        if (!result.isValid()) {
            cmdEntity.setCommandStatus(CommandStatus.FAILED);
        }
        commandsRepository.persistCommand(cmdEntity);
    }

    static class RejectedExecutionFuture implements Future<VdcReturnValueBase> {

        VdcReturnValueBase retValue;

        RejectedExecutionFuture() {
            retValue = new VdcReturnValueBase();
            retValue.setSucceeded(false);
            EngineFault fault = new EngineFault();
            fault.setError(EngineError.ResourceException);
            fault.setMessage(Backend.getInstance()
                    .getVdsErrorsTranslator()
                    .translateErrorTextSingle(fault.getError().toString()));
            retValue.setFault(fault);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public VdcReturnValueBase get() throws InterruptedException, ExecutionException {
            return retValue;
        }

        @Override
        public VdcReturnValueBase get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return retValue;
        }
    }

}
