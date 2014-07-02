package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.compat.Guid;

public interface CommandContextsCache {

    public CommandContext get(Guid commandId);

    public void remove(Guid commandId);

    public void put(Guid commandId, CommandContext context);

}
