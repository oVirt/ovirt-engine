package org.ovirt.engine.core.bll.tasks.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandCRUDOperations {

    boolean hasCommandEntitiesWithRootCommandId(Guid rootCommandId);

    CommandEntity createCommandEntity(Guid cmdId, VdcActionType actionType, VdcActionParametersBase params);

    List<Guid> getChildCommandIds(Guid commandId);

    List<Guid> getChildCommandIds(Guid commandId, VdcActionType childActionType, CommandStatus status);

    List<Guid> getCommandIdsBySessionSeqId(long engineSessionSeqId);

    CommandEntity getCommandEntity(Guid commandId);

    CommandStatus getCommandStatus(Guid commandId);

    void persistCommand(CommandEntity cmdEntity);

    void persistCommand(CommandEntity cmdEntity, CommandContext cmdContext);

    void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities);

    List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId);

    List<Guid> getCommandIdsByEntityId(Guid entityId);

    CommandBase<?> retrieveCommand(Guid commandId);

    void removeCommand(Guid commandId);

    void removeAllCommandsInHierarchy(Guid commandId);

    void removeAllCommandsBeforeDate(DateTime cutoff);

    void updateCommandData(Guid commandId, Map<String, Serializable> data);

    void updateCommandStatus(Guid commandId, CommandStatus status);

    void updateCommandExecuted(Guid commandId);

    List<CommandEntity> getChildCmdsByRootCmdId(Guid cmdId);
}
