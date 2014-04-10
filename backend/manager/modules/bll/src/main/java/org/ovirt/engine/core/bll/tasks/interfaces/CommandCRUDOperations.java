package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandCRUDOperations {
    public CommandEntity getCommandEntity(Guid commandId);
    public void persistCommand(CommandEntity cmdEntity);
    public void persistCommand(Guid commandId, Guid rootCommandId, VdcActionType actionType, VdcActionParametersBase params, CommandStatus status);
    public CommandBase<?> retrieveCommand(Guid commandId);
    public void removeCommand(Guid commandId);
    public void removeAllCommandsBeforeDate(DateTime cutoff);
    public void updateCommandStatus(Guid commandId, AsyncTaskType taskType, CommandStatus status);
}
