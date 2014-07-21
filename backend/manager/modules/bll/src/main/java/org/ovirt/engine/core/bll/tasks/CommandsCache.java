package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

import java.util.Set;

public interface CommandsCache {

    public CommandEntity get(Guid commandId);

    public Set<Guid> keySet();

    public void remove(Guid commandId);

    public void put(CommandEntity cmdEntity);

    public void removeAllCommandsBeforeDate(DateTime cutoff);

    public void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status);

    public void updateCommandExecuted(Guid commandId);

    public void updateCallBackNotified(Guid commandId);

}
