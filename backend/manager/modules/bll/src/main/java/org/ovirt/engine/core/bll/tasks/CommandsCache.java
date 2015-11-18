package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandsCache {

    CommandEntity get(Guid commandId);

    Set<Guid> keySet();

    void remove(Guid commandId);

    void put(CommandEntity cmdEntity);

    void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities);

    List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId);

    List<Guid> getCommandIdsByEntityId(Guid entityId);

    void removeAllCommandsBeforeDate(DateTime cutoff);

    void updateCommandData(Guid commandId, Map<String, Serializable> data);

    void updateCommandStatus(Guid commandId, CommandStatus status);

    void updateCommandExecuted(Guid commandId);

    void updateCallbackNotified(Guid commandId);

    List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId);
}
