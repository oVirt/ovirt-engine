package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandCRUDOperations {
    public boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId);
    public CommandEntity createCommandEntity(Guid cmdId, VdcActionType actionType, VdcActionParametersBase params);
    public List<Guid> getChildCommandIds(Guid commandId);
    public CommandEntity getCommandEntity(Guid commandId);
    public CommandStatus getCommandStatus(Guid commandId);
    public List<CommandEntity> getCommandsWithCallBackEnabled();
    public void persistCommand(CommandEntity cmdEntity);
    public void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext);
    public CommandBase<?> retrieveCommand(Guid commandId);
    public void removeCommand(Guid commandId);
    public void removeAllCommandsInHierarchy(Guid commandId);
    public void removeAllCommandsBeforeDate(DateTime cutoff);
    public void updateCommandStatus(Guid commandId, CommandStatus status);
    public void updateCommandExecuted(Guid commandId);
    public void updateCallBackNotified(Guid commandId);
    public List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId);
}
