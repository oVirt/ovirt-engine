package org.ovirt.engine.core.bll.tasks;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;

public interface CommandsCache {

    CommandEntity get(Guid commandId);

    Set<Guid> keySet();

    void remove(Guid commandId);

    void put(CommandEntity cmdEntity);

    void removeAllCommandsBeforeDate(DateTime cutoff);

    void updateCommandStatus(Guid commandId, CommandStatus status);

    void updateCommandExecuted(Guid commandId);

    void updateCallbackNotified(Guid commandId);

    List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId);
}
