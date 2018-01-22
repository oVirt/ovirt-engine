package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.aaa.SsoSessionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandContextsCache;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandsRepository {

    private static final Logger log = LoggerFactory.getLogger(CommandsRepository.class);
    private final ConcurrentMap<Guid, CallbackTiming> callbacksTiming;
    private final CommandsCache commandsCache;
    private final CommandContextsCache contextsCache;
    private final ConcurrentHashMap<Guid, List<Guid>> childHierarchy;
    private final ConcurrentMap<Guid, CoCoEventSubscriber> subscriptions;
    private final Object LOCK;
    private volatile boolean childHierarchyInitialized;
    @Inject
    private AsyncTaskDao asyncTaskDao;

    @Inject
    public CommandsRepository(CommandsCache commandsCache, CommandContextsCache contextsCache) {
        this.commandsCache = commandsCache;
        this.contextsCache = contextsCache;

        callbacksTiming = new ConcurrentHashMap<>();
        childHierarchy = new ConcurrentHashMap<>();
        subscriptions = new ConcurrentHashMap<>();
        LOCK = new Object();
    }

    public void addToCallbackMap(CommandEntity cmdEntity) {
        if (!callbacksTiming.containsKey(cmdEntity.getId())) {
            CommandBase<?> cmd = retrieveCommand(cmdEntity.getId());
            if (cmd != null && cmd.getCallback() != null) {
                CallbackTiming callbackTiming = new CallbackTiming(cmd.getCallback(),
                        Config.<Long>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds));
                if (cmdEntity.isWaitingForEvent()) {
                    long waitOnEventEndTime = System.currentTimeMillis()
                            + TimeUnit.MINUTES.toMillis(Config.<Integer>getValue(ConfigValues.CoCoWaitForEventInMinutes));
                    callbackTiming.setWaitOnEventEndTime(waitOnEventEndTime);
                }
                addToCallbackMap(cmdEntity.getId(), callbackTiming);
            }
        }
    }

    /**
     * This method is responsible to update the statuses of all the commands that ACTIVE and persisted
     * but aren't managed to status that'll reflect that their execution was ended with failure.
     */
    public void handleUnmanagedCommands() {
        List<AsyncTask> asyncTasks = asyncTaskDao.getAll();
        Set<Guid> asyncTaskManagerManagedCommands = asyncTasks.stream().filter(x -> x.getVdsmTaskId() != null)
                .map(x -> x.getRootCommandId()).collect(Collectors.toSet());
        asyncTaskManagerManagedCommands.addAll(asyncTasks.stream().filter(x -> x.getVdsmTaskId() != null)
                .map(x -> x.getCommandId()).collect(Collectors.toSet()));

        // this will update all the commands that aren't managed by callback/async task manager and are active
        // and will set their status to ENDED_WITH_FAILURE.
        getCommands(false).stream()
                .filter(x -> !x.isCallbackEnabled())
                .filter(x -> x.getCommandStatus() == CommandStatus.ACTIVE)
                .filter(x -> !asyncTaskManagerManagedCommands.contains(x.getId()))
                .forEach(x -> commandsCache.updateCommandStatus(x.getId(), CommandStatus.ENDED_WITH_FAILURE));

        // active commands managed by callbacks and not managed by async task manager need to reacquire locks
        // on engine restart
        getCommands(false).stream()
                .filter(x -> x.isCallbackEnabled())
                .filter(x -> !x.isCallbackNotified())
                .filter(x -> x.getCommandStatus().isDuringExecution())
                .filter(x -> !asyncTaskManagerManagedCommands.contains(x.getId()))
                .map(x -> retrieveCommand(x.getId()))
                .filter(Objects::nonNull)
                .forEach(CommandBase::reacquireLocks);
    }

    public void addToCallbackMap(Guid commandId, CallbackTiming callbackTiming) {
        callbacksTiming.put(commandId, callbackTiming);
    }

    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        initChildHierarchy();
        if (Guid.isNullOrEmpty(cmdEntity.getId())) {
            return;
        }
        persistCommand(cmdEntity);
        if (cmdContext != null) {
            contextsCache.put(cmdEntity.getId(), cmdContext);
        }
    }

    public void persistCommand(CommandEntity cmdEntity) {
        if (Guid.isNullOrEmpty(cmdEntity.getId())) {
            return;
        }
        CommandEntity existingCmdEntity = commandsCache.get(cmdEntity.getId());
        if (existingCmdEntity != null) {
            cmdEntity.setExecuted(existingCmdEntity.isExecuted());
            cmdEntity.setCallbackNotified(existingCmdEntity.isCallbackNotified());
        }
        commandsCache.put(cmdEntity);
        // check if callback is enabled or if parent command has callback enabled
        if (cmdEntity.isCallbackEnabled() ||
                (!Guid.isNullOrEmpty(cmdEntity.getParentCommandId()) &&
                        commandsCache.get(cmdEntity.getParentCommandId()) != null &&
                        commandsCache.get(cmdEntity.getParentCommandId()).isCallbackEnabled()
                )) {
            buildCmdHierarchy(cmdEntity);
            if (!cmdEntity.isCallbackNotified()) {
                addToCallbackMap(cmdEntity);
            }
        }
    }

    public CommandContext retrieveCommandContext(Guid cmdId) {
        return contextsCache.get(cmdId);
    }

    public CommandBase<?> retrieveCommand(Guid commandId) {
        return retrieveCommand(commandsCache.get(commandId), retrieveCommandContext(commandId));
    }

    public void updateCommandStatus(final Guid commandId, final CommandStatus status) {
        commandsCache.updateCommandStatus(commandId, status);
    }

    private CommandBase<?> retrieveCommand(CommandEntity cmdEntity, CommandContext cmdContext) {
        CommandBase<?> command = null;
        if (cmdEntity != null) {
            if (cmdContext == null) {
                cmdContext = new CommandContext(new EngineContext()).withExecutionContext(new ExecutionContext());
            }
            command = CommandsFactory.createCommand(cmdEntity.getCommandType(), cmdEntity.getCommandParameters(), cmdContext);
            if (command != null) {
                command.setCommandStatus(cmdEntity.getCommandStatus(), false);
                command.setCommandData(cmdEntity.getData());
                command.setReturnValue(cmdEntity.getReturnValue());
                if (!Guid.isNullOrEmpty(cmdEntity.getParentCommandId()) &&
                        !cmdEntity.getParentCommandId().equals(cmdEntity.getId()) &&
                        command.getParameters().getParentParameters() == null) {
                    CommandBase<?> parentCommand = retrieveCommand(cmdEntity.getParentCommandId());
                    if (parentCommand != null) {
                        command.getParameters().setParentParameters(parentCommand.getParameters());
                    }
                }
            }
        }
        return command;
    }

    public CommandStatus getCommandStatus(final Guid commandId) {
        CommandEntity cmdEntity = commandsCache.get(commandId);
        if (cmdEntity != null) {
            return cmdEntity.getCommandStatus();
        }
        return CommandStatus.UNKNOWN;
    }

    public CommandEntity getCommandEntity(Guid commandId) {
        return Guid.isNullOrEmpty(commandId) ? null : commandsCache.get(commandId);
    }

    public void updateCallbackNotified(final Guid commandId) {
        commandsCache.updateCallbackNotified(commandId);
    }

    /**
     * @param onlyWithCallbackEnabled Specifies if the returned commands' callbacks are enabled or not.
     * @return Returns commands with callback enabled or disabled, based on given parameter
     */
    public List<CommandEntity> getCommands(boolean onlyWithCallbackEnabled) {
        List<CommandEntity> cmdEntities = new ArrayList<>();
        CommandEntity cmdEntity;
        for (Guid cmdId : commandsCache.keySet()) {
            cmdEntity = commandsCache.get(cmdId);
            if (!onlyWithCallbackEnabled || commandsCache.get(cmdId).isCallbackEnabled()) {
                cmdEntities.add(cmdEntity);
            }
        }
        return cmdEntities;
    }

    public void removeCommand(Guid commandId) {
        commandsCache.remove(commandId);
        contextsCache.remove(commandId);
        updateCmdHierarchy(commandId);
    }

    private void initChildHierarchy() {
        if (!childHierarchyInitialized) {
            synchronized(LOCK) {
                if (!childHierarchyInitialized) {
                    childHierarchy.clear();
                    getCommands(false).forEach(this::buildCmdHierarchy);
                }
                childHierarchyInitialized = true;
            }
        }
    }

    private void buildCmdHierarchy(CommandEntity cmdEntity) {
        if (!Guid.isNullOrEmpty(cmdEntity.getParentCommandId()) && !cmdEntity.getId().equals(cmdEntity.getParentCommandId())) {
            childHierarchy.putIfAbsent(cmdEntity.getParentCommandId(), new ArrayList<>());
            if (!childHierarchy.get(cmdEntity.getParentCommandId()).contains(cmdEntity.getId())) {
                childHierarchy.get(cmdEntity.getParentCommandId()).add(cmdEntity.getId());
            }
        }
    }

    private void updateCmdHierarchy(Guid cmdId) {
        for (List<Guid> childIds : childHierarchy.values()) {
            if (childIds.contains(cmdId)) {
                childIds.remove(cmdId);
                break;
            }
        }
        if (childHierarchy.containsKey(cmdId) && childHierarchy.get(cmdId).size() == 0) {
            childHierarchy.remove(cmdId);
        }
    }

    public void removeAllCommandsBeforeDate(final DateTime cutoff) {
        commandsCache.removeAllCommandsBeforeDate(cutoff);
        synchronized(LOCK) {
            childHierarchyInitialized = false;
        }
    }

    public List<Guid> getChildCommandIds(Guid cmdId) {
        initChildHierarchy();
        if (childHierarchy.containsKey(cmdId)) {
            return childHierarchy.get(cmdId);
        }
        return Collections.emptyList();
    }

    public ConcurrentMap<Guid, CallbackTiming> getCallbacksTiming() {
        return callbacksTiming;
    }

    public void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities) {
        commandsCache.persistCommandAssociatedEntities(cmdAssociatedEntities);
    }

    public List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId) {
        return commandsCache.getChildCmdsByParentCmdId(cmdId);
    }

    public List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return commandsCache.getCommandIdsByEntityId(entityId);
    }

    public List<Guid> getCommandIdsBySessionSeqId(long engineSessionSeqId) {
        List<Guid> cmdIds = new ArrayList<>();
        CommandEntity cmdEntity;

        for (Guid cmdId : commandsCache.keySet()) {
            cmdEntity = commandsCache.get(cmdId);
            if (cmdEntity != null && cmdEntity.getEngineSessionSeqId() != SsoSessionUtils.EMPTY_SESSION_SEQ_ID &&
                    cmdEntity.getEngineSessionSeqId() == engineSessionSeqId) {
                cmdIds.add(cmdId);
            }
        }
        return cmdIds;
    }

    public List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return commandsCache.getCommandAssociatedEntities(cmdId);
    }

    public void updateCommandData(Guid commandId, Map<String, Serializable> data) {
        commandsCache.updateCommandData(commandId, data);
    }

    public void updateCommandExecuted(Guid commandId) {
        commandsCache.updateCommandExecuted(commandId);
    }

    public boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId) {
        CommandEntity cmdEntity;
        for (Guid cmdId : commandsCache.keySet()) {
            cmdEntity = commandsCache.get(cmdId);
            if (cmdEntity != null && !Guid.isNullOrEmpty(cmdEntity.getRootCommandId()) &&
                    !cmdEntity.getRootCommandId().equals(cmdId) &&
                    cmdEntity.getRootCommandId().equals(rootCommandId)) {
                return true;
            }
        }
        return false;
    }

    public CallbackTiming getCallbackTiming(Guid commandId) {
        if (Guid.isNullOrEmpty(commandId)) {
            return null;
        }

        return callbacksTiming.get(commandId);
    }

    public void markExpiredCommandsAsFailure() {
        for (Guid commandId : callbacksTiming.keySet()) {
            List<Guid> childCmdIds = getChildCommandIds(commandId);
            if (childCmdIds.isEmpty()) {
                markExpiredCommandAsFailure(commandId);
            } else {
                childCmdIds.forEach(this::markExpiredCommandAsFailure);
            }
        }
    }

    private void markExpiredCommandAsFailure(Guid cmdId) {
        CommandEntity cmdEntity = getCommandEntity(cmdId);
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
                updateCommandStatus(cmdId, CommandStatus.FAILED);
            }
        }
    }

    public void addEventSubscription(CommandEntity command, CoCoEventSubscriber subscription) {
        subscriptions.putIfAbsent(command.getId(), subscription);
    }

    public void removeEventSubscription(Guid commandId) {
        CoCoEventSubscriber subscriber = subscriptions.remove(commandId);
        if (subscriber != null) {
            subscriber.cancel();
        }
    }
}
