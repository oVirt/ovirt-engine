package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.Guid;

public class CommandsCacheImpl implements CommandsCache {

    private static final String COMMAND_MAP_NAME = "commandMap";
    CacheWrapper<Guid, CommandEntity> commandMap;

    public CommandsCacheImpl() {
        commandMap = CacheProviderFactory.<Guid, CommandEntity> getCacheWrapper(COMMAND_MAP_NAME);
    }

    @Override
    public CommandEntity get(Guid commandId) {
        return commandMap.get(commandId);
    }

    @Override
    public void remove(Guid commandId) {
        commandMap.remove(commandId);
    }

    @Override
    public void put(Guid commandId, Guid parentCommandId, SPMAsyncTask task) {
        commandMap.put(commandId, buildCommandEntity(commandId, parentCommandId, task));
    }

    private CommandEntity buildCommandEntity(Guid commandId, Guid parentCommandId, SPMAsyncTask task) {
        CommandEntity entity = new CommandEntity();
        entity.setId(commandId);
        entity.setParentCommandId(parentCommandId);
        CommandEntityUtils.setParameters(entity, task.getParameters().getDbAsyncTask().getActionParameters());
        return entity;
    }
}
