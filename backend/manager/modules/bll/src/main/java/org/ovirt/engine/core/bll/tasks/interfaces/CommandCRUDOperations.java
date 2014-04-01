package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandCRUDOperations {
    public abstract void persistCommand(Guid commandId, Guid rootCommandId, VdcActionType actionType, VdcActionParametersBase params, CommandStatus status);
    public abstract CommandBase<?> retrieveCommand(Guid commandId);
    public abstract void removeCommand(Guid commandId);
    public abstract void removeAllCommandsBeforeDate(DateTime cutoff);
    public abstract void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status);
}
