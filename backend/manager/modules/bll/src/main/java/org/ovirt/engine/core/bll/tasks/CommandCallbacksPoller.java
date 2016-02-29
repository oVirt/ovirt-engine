package org.ovirt.engine.core.bll.tasks;

import static org.ovirt.engine.core.bll.tasks.CommandsRepository.CommandContainer;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
    private boolean cmdExecutorInitialized;
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
        schedulerUtil.scheduleAFixedDelayJob(this,
                "invokeCallbackMethods",
                new Class[]{},
                new Object[]{},
                pollingRate,
                pollingRate,
                TimeUnit.SECONDS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    @OnTimerMethodAnnotation("invokeCallbackMethods")
    public void invokeCallbackMethods() {
        initCommandExecutor();
        Iterator<Entry<Guid, CommandContainer>> iterator = commandsRepository.getCommandContainers().iterator();
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
                if (CommandStatus.FAILED.equals(status)
                        || (CommandStatus.SUCCEEDED.equals(status) && !errorInCallback)) {
                    commandsRepository.updateCallbackNotified(cmdId);
                    iterator.remove();
                    CommandEntity cmdEntity = commandsRepository.getCommandEntity(entry.getKey());
                    if (cmdEntity != null) {
                        // When a child finishes, its parent's callback should execute shortly thereafter
                        CommandContainer rootCmdContainer =
                                commandsRepository.getCommandCallbackContainer(cmdEntity.getRootCommandId());
                        if (rootCmdContainer != null) {
                            rootCmdContainer.setInitialDelay(pollingRate);
                            rootCmdContainer.setRemainingDelay(pollingRate);
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
}
