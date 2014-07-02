package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandContextsCache;
import org.ovirt.engine.core.compat.Guid;

public class CommandContextsCacheImpl implements CommandContextsCache {

    private static final String COMMAND_CONTEXT_MAP_NAME = "commandContextMap";
    CacheWrapper<Guid, CommandContext> contextsMap;

    public CommandContextsCacheImpl() {
        contextsMap = CacheProviderFactory.<Guid, CommandContext> getCacheWrapper(COMMAND_CONTEXT_MAP_NAME);
    }

    @Override
    public CommandContext get(Guid commandId) {
        return contextsMap.get(commandId);
    }

    @Override
    public void remove(final Guid commandId) {
        contextsMap.remove(commandId);
    }

    @Override
    public void put(final Guid cmdId, final CommandContext context) {
        contextsMap.put(cmdId, context);
    }

}
