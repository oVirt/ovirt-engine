package org.ovirt.engine.core.bll.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCallbacksPoller implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(CommandCallbacksPoller.class);
    private int pollingRate;

    @Inject
    private CommandsRepository commandsRepository;

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    CommandCallbacksPoller() {
    }

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        pollingRate = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds);
        initCommandExecutor();
        schedulerUtil.scheduleAFixedDelayJob(this,
                "invokeCallbackMethods",
                new Class[]{},
                new Object[]{},
                pollingRate,
                pollingRate,
                TimeUnit.SECONDS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private boolean endCallback(Guid cmdId, CommandCallback callback, CommandStatus status) {
        try {
            if (status == CommandStatus.FAILED) {
                callback.onFailed(cmdId, getChildCommandIds(cmdId));
            } else {
                callback.onSucceeded(cmdId, getChildCommandIds(cmdId));
            }
        } catch (Exception ex) {
            if (callback.shouldRepeatEndMethodsOnFail(cmdId)) {
                log.error("Failed invoking callback end method '{}' for command '{}' with exception '{}', the callback"
                        + " is marked for end method retries",
                        getCallbackMethod(status),
                        cmdId,
                        ex.getMessage());
                log.debug("Exception", ex);
                return true;
            }

            throw ex;
        }

        return false;
    }

    private List<Guid> getChildCommandIds(Guid cmdId) {
        return commandsRepository.getChildCommandIds(cmdId);
    }

    @OnTimerMethodAnnotation("invokeCallbackMethods")
    public void invokeCallbackMethods() {
        Iterator<Entry<Guid, CallbackTiming>> iterator = commandsRepository.getCallbacksTiming().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Guid, CallbackTiming> entry = iterator.next();

            Guid cmdId = entry.getKey();
            CallbackTiming callbackTiming = entry.getValue();

            CommandEntity commandEntity = commandsRepository.getCommandEntity(cmdId);
            if (commandEntity != null && updateCommandWaitingForEvent(commandEntity, callbackTiming)) {
                continue;
            } else {
                // Decrement counter; execute if it reaches 0
                callbackTiming.setRemainingDelay(callbackTiming.getRemainingDelay() - pollingRate);
                if (callbackTiming.getRemainingDelay() > 0) {
                    continue;
                }
            }

            CommandCallback callback = callbackTiming.getCallback();
            CommandStatus status = commandsRepository.getCommandStatus(cmdId);
            boolean runCallbackAgain = false;
            boolean errorInCallback = false;
            try {
                switch (status) {
                    case FAILED:
                    case SUCCEEDED:
                        runCallbackAgain = endCallback(cmdId, callback, status);
                        break;
                    case ACTIVE:
                        if (commandEntity != null && commandEntity.isExecuted()) {
                            callback.doPolling(cmdId, getChildCommandIds(cmdId));
                        }
                        break;
                    case EXECUTION_FAILED:
                        if (callback.pollOnExecutionFailed()) {
                            callback.doPolling(cmdId, getChildCommandIds(cmdId));
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                errorInCallback = true;
                handleError(ex, status, cmdId);
            } finally {
                if ((CommandStatus.FAILED == status || (CommandStatus.SUCCEEDED == status && !errorInCallback))
                        && !runCallbackAgain) {
                    commandsRepository.updateCallbackNotified(cmdId);
                    iterator.remove();
                    CommandEntity cmdEntity = commandsRepository.getCommandEntity(entry.getKey());
                    if (cmdEntity != null) {
                        // When a child finishes, its parent's callback should execute shortly thereafter
                        CallbackTiming rootCmdContainer =
                                commandsRepository.getCallbackTiming(cmdEntity.getRootCommandId());
                        if (rootCmdContainer != null) {
                            rootCmdContainer.setInitialDelay(pollingRate);
                            rootCmdContainer.setRemainingDelay(pollingRate);
                        }
                    }
                } else if (status != commandsRepository.getCommandStatus(cmdId)) {
                    callbackTiming.setInitialDelay(pollingRate);
                    callbackTiming.setRemainingDelay(pollingRate);
                } else {
                    int maxDelay = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingRateInSeconds);
                    callbackTiming.setInitialDelay(Math.min(maxDelay, callbackTiming.getInitialDelay() * 2));
                    callbackTiming.setRemainingDelay(callbackTiming.getInitialDelay());
                }
            }
        }

        commandsRepository.markExpiredCommandsAsFailure();
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
        for (CommandEntity cmdEntity : commandsRepository.getCommands(true)) {
            if (!cmdEntity.isExecuted() &&
                    cmdEntity.getCommandStatus() != CommandStatus.FAILED &&
                    cmdEntity.getCommandStatus() != CommandStatus.EXECUTION_FAILED &&
                    cmdEntity.getCommandStatus() != CommandStatus.ENDED_WITH_FAILURE
                    ) {
                CommandBase<?> failedCommand = commandsRepository.retrieveCommand(cmdEntity.getId());
                failedCommand.setCommandStatus(CommandStatus.EXECUTION_FAILED);
            }

            if (!cmdEntity.isCallbackNotified()) {
                commandsRepository.addToCallbackMap(cmdEntity);
            }
        }
    }

    /**
     * Checks and updates the command if the time to wait for an event to arrive has expired, the command will be move
     * to polling mode
     *
     * @param cmdEntity
     *            the entity which represents the command
     * @param callbackTiming
     *            the container of the callback and its timing
     * @return {@code true} if command's callback is waiting for an event, else {@code false}
     */
    private boolean updateCommandWaitingForEvent(CommandEntity cmdEntity, CallbackTiming callbackTiming) {
        if (cmdEntity.isWaitingForEvent()) {
            if (callbackTiming.getWaitOnEventEndTime() < System.currentTimeMillis()) {
                log.info("The command '{}' reached its event's waiting timeout and will be moved to polling mode",
                        cmdEntity.getId());
                commandsRepository.removeEventSubscription(cmdEntity.getId());
                cmdEntity.setWaitingForEvent(false);
                return false;
            }
            return true;
        }
        return false;
    }
}
