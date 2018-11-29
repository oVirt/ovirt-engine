package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCallbacksPoller implements BackendService {

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(CommandCallbacksPoller.class);
    private long pollingRate;

    @Inject
    private CommandsRepository commandsRepository;

    private ConcurrentMap<Guid, AtomicInteger> callbackInvocationMap = new ConcurrentHashMap<>();

    private int repeatEndMethodsOnFailMaxRetries;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        pollingRate = Config.<Long>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds);
        repeatEndMethodsOnFailMaxRetries = Config.<Integer>getValue(ConfigValues.RepeatEndMethodsOnFailMaxRetries);
        initCommandExecutor();
        executor.scheduleWithFixedDelay(this::invokeCallbackMethods,
                pollingRate,
                pollingRate,
                TimeUnit.SECONDS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private boolean endCallback(Guid cmdId, CommandCallback callback, CommandStatus status) {
        try {
            boolean shouldRepeatEndMethodsOnFail = callback.shouldRepeatEndMethodsOnFail(cmdId);
            if (shouldRepeatEndMethodsOnFail) {
                callbackInvocationMap.putIfAbsent(cmdId, new AtomicInteger(0));
                callbackInvocationMap.get(cmdId).getAndIncrement();
            }
            if (status == CommandStatus.FAILED) {
                callback.onFailed(cmdId, getChildCommandIds(cmdId));
            } else {
                callback.onSucceeded(cmdId, getChildCommandIds(cmdId));
            }
            if (shouldRepeatEndMethodsOnFail) {
                callbackInvocationMap.remove(cmdId);
            }
        } catch (Throwable ex) {
            if (callback.shouldRepeatEndMethodsOnFail(cmdId)) {
                if (callbackInvocationMap.getOrDefault(cmdId, new AtomicInteger(0)).get() >
                        repeatEndMethodsOnFailMaxRetries) {
                    callbackInvocationMap.remove(cmdId);
                    callback.finalizeCommand(cmdId, status == CommandStatus.SUCCEEDED);
                    log.error("Failed invoking callback end method '{}' for command '{}' with exception '{}', the"
                                    + " callback is marked for end method retries but max number of retries have been"
                                    + " attempted. The command will be marked as Failed.",
                            getCallbackMethod(status),
                            cmdId,
                            ex.getMessage());
                    throw ex;
                }
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
        return new ArrayList<>(commandsRepository.getChildCommandIds(cmdId));
    }

    private void invokeCallbackMethods() {
        try {
            invokeCallbackMethodsImpl();
        } catch (Throwable t) {
            logInvocationCallbackError(t);
        }
    }

    /**
     * Ignore any errors that occur during logging. Any exception thrown in invokeCallbackMethods will stop the
     * method from being invoked again, so we need to wrap all statements in try catch blocks including logging to
     * eliminate any possibility of exception in the method.
     * @param throwable
     *            The exception that needs to be logged
     */
    private void logInvocationCallbackError(Throwable throwable) {
        try {
            log.error("Exception in invokeCallbackMethods: {}", ExceptionUtils.getRootCauseMessage(throwable));
            log.debug("Exception", throwable);
        } catch (Throwable t) {
            // log the stacktrace to the stdout as the exception was raised somewhere inside logging code
            t.printStackTrace(System.out);
        }
    }

    private void invokeCallbackMethodsImpl() {
        Iterator<Entry<Guid, CallbackTiming>> iterator = commandsRepository.getCallbacksTiming().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Guid, CallbackTiming> entry = iterator.next();

            Guid cmdId = entry.getKey();
            CallbackTiming callbackTiming = entry.getValue();
            CommandEntity commandEntity = commandsRepository.getCommandEntity(cmdId);
            CorrelationIdTracker.setCorrelationId(commandEntity != null
                    ? commandEntity.getCommandParameters().getCorrelationId() : null);
            if (commandEntity != null && updateCommandWaitingForEvent(commandEntity, callbackTiming)) {
                continue;
            }

            // Decrement counter; execute if it reaches 0
            callbackTiming.setRemainingDelay(callbackTiming.getRemainingDelay() - pollingRate);
            ActionType cmdActionType = commandEntity == null ? ActionType.Unknown : commandEntity.getCommandType();
            if (callbackTiming.getRemainingDelay() > 0) {
                log.debug("The command {} ({}) has remaining delay {}, polling will be skipped.",
                        cmdActionType,
                        cmdId,
                        callbackTiming.getRemainingDelay());
                continue;
            }

            CommandCallback callback = callbackTiming.getCallback();
            CommandStatus status = commandsRepository.getCommandStatus(cmdId);
            log.debug("Command {} ({}) in status {}", cmdActionType, cmdId, status);
            boolean runCallbackAgain = false;
            boolean errorInCallback = false;
            try {
                switch (status) {
                    case FAILED:
                    case SUCCEEDED:
                        runCallbackAgain = endCallback(cmdId, callback, status);
                        break;
                    case ACTIVE:
                        if (commandEntity == null) {
                            log.info("Not invoking command's {} doPolling method command entity is null, callback is {}.",
                                    cmdId,
                                    callbackTiming.getCallback() == null ? "NULL" : callbackTiming.getCallback().getClass().getCanonicalName());
                        } else if (commandEntity.isExecuted()) {
                            log.debug("Invoking command's {} ({}) doPolling method.", cmdActionType, cmdId);
                            callback.doPolling(cmdId, getChildCommandIds(cmdId));
                        }
                        break;
                    case EXECUTION_FAILED:
                        if (callback.pollOnExecutionFailed()) {
                            log.debug("Invoking command's {} ({}) doPolling method.", cmdActionType, cmdId);
                            callback.doPolling(cmdId, getChildCommandIds(cmdId));
                        } else {
                            log.info("Not invoking command's {} ({}) doPolling method callback's pollOnExecutionFailed is false.",
                                    cmdActionType, cmdId);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Throwable ex) {
                errorInCallback = true;
                log.info("Exception in invoking callback of command {} ({}): {}",
                        cmdActionType,
                        cmdId,
                        ExceptionUtils.getRootCauseMessage(ex));
                log.debug("Exception", ex);
                handleError(ex, status, cmdId);
            } finally {
                if ((CommandStatus.FAILED == status || (CommandStatus.SUCCEEDED == status && !errorInCallback))
                        && !runCallbackAgain) {
                    log.debug("Callback of command {} ({}) has been notified, removing command from command repository.",
                            cmdActionType, cmdId);
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
                    log.debug("Command {} ({}) status {} has been updated to {}, command will be polled again.",
                            cmdActionType, cmdId,
                            commandsRepository.getCommandStatus(cmdId),
                            status);
                    callbackTiming.setInitialDelay(pollingRate);
                    callbackTiming.setRemainingDelay(pollingRate);
                } else {
                    log.debug("Command {} ({}) will be polled again, updating initial and remaining delay.", cmdActionType, cmdId);
                    long maxDelay = Config.<Long>getValue(ConfigValues.AsyncCommandPollingRateInSeconds);
                    callbackTiming.setInitialDelay(Math.min(maxDelay, callbackTiming.getInitialDelay() * 2));
                    callbackTiming.setRemainingDelay(callbackTiming.getInitialDelay());
                }
            }
        }
        CorrelationIdTracker.setCorrelationId(null);
        commandsRepository.markExpiredCommandsAsFailure();
    }

    private void handleError(Throwable ex, CommandStatus status, Guid cmdId) {
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
                commandsRepository.updateCommandStatus(cmdEntity.getId(), CommandStatus.EXECUTION_FAILED);
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
            log.debug("The command '{}' is waiting for event and will not be polled until event or timeout",
                    cmdEntity.getId());
            return true;
        }
        return false;
    }
}
