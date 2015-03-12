package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.compat.Guid;

public interface CommandContextsCache {

    CommandContext get(Guid commandId);

    void remove(Guid commandId);

    void put(Guid commandId, CommandContext context);

}
