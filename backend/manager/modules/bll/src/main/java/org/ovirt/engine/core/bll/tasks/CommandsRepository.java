package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.CommandsFactory;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandContextsCache;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

@Singleton
public class CommandsRepository {

    private final Map<Guid, CommandContainer> cmdCallbacksById;
    private final CommandsCache commandsCache;
    private final CommandContextsCache contextsCache;
    private final ConcurrentHashMap<Guid, List<Guid>> childHierarchy;
    private Integer pollingRate = Config.<Integer>getValue(ConfigValues.AsyncCommandPollingLoopInSeconds);
    private final Object LOCK;
    private volatile boolean childHierarchyInitialized;

    public CommandsRepository() {
        cmdCallbacksById = new ConcurrentHashMap<>();
        commandsCache = new CommandsCacheImpl();
        contextsCache = new CommandContextsCacheImpl(commandsCache);
        childHierarchy = new ConcurrentHashMap<>();
        LOCK = new Object();
    }

    public void addToCallbackMap(CommandEntity cmdEntity) {
        if (!cmdCallbacksById.containsKey(cmdEntity.getId())) {
            CommandBase<?> cmd = retrieveCommand(cmdEntity.getId());
            if (cmd != null && cmd.getCallback() != null) {
                cmdCallbacksById.put(cmdEntity.getId(), new CommandContainer(cmd.getCallback(), pollingRate));
            }
        }
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

    public CommandBase<?> retrieveCommand(Guid commandId) {
        return retrieveCommand(commandsCache.get(commandId), contextsCache.get(commandId));
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

    public Map<Guid, CommandContainer> getCommandsCallback() {
        return cmdCallbacksById;
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

    static class CommandContainer {
        // Total delay between callback executions
        private int initialDelay;

        // Remaining delay to next callback execution
        private int remainingDelay;
        private CommandCallback callback;

        public CommandContainer(CommandCallback callback, int executionDelay) {
            this.callback = callback;
            this.initialDelay = executionDelay;
            this.remainingDelay = executionDelay;
        }

        public int getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(int initialDelay) {
            this.initialDelay = initialDelay;
        }

        public int getRemainingDelay() {
            return remainingDelay;
        }

        public void setRemainingDelay(int remainingDelay) {
            this.remainingDelay = remainingDelay;
        }

        public CommandCallback getCallback() {
            return callback;
        }

        public void setCallback(CommandCallback callback) {
            this.callback = callback;
        }
    }
}
