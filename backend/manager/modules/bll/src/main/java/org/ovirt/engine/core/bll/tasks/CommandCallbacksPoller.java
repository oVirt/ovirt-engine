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
        } catch (Exception ex) {
            if (callback.shouldRepeatEndMethodsOnFail(cmdId)) {
                if (callbackInvocationMap.getOrDefault(cmdId, new AtomicInteger(0)).get() >
                        repeatEndMethodsOnFailMaxRetries) {
                    callbackInvocationMap.remove(cmdId);
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
            log.error("Exception in invokeCallbackMethods: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
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
            if (callbackTiming.getRemainingDelay() > 0) {
                continue;
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
                    long maxDelay = Config.<Long>getValue(ConfigValues.AsyncCommandPollingRateInSeconds);
                    callbackTiming.setInitialDelay(Math.min(maxDelay, callbackTiming.getInitialDelay() * 2));
                    callbackTiming.setRemainingDelay(callbackTiming.getInitialDelay());
                }
            }
        }
        CorrelationIdTracker.setCorrelationId(null);
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
            return true;
        }
        return false;
    }
}
