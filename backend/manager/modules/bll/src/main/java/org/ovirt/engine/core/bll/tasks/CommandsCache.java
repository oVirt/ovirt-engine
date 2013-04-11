package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.Guid;

public interface CommandsCache {

    public CommandEntity get(Guid commandId);

    public void remove(Guid commandId);

    public void put(Guid commandId, Guid parentCommandId, SPMAsyncTask task);

}
