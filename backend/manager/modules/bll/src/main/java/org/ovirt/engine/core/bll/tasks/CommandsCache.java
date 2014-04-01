package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandsCache {

    public CommandEntity get(Guid commandId);

    public void remove(Guid commandId);

    public void put(Guid commandId, Guid rootCommandId, VdcActionType actionType, VdcActionParametersBase params, CommandStatus status);

    public void removeAllCommandsBeforeDate(DateTime cutoff);

    public void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status);

}
