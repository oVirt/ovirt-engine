package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public interface CommandEntityDao extends GenericDao<CommandEntity, Guid> {

    void saveOrUpdate(CommandEntity commandEntity);

    void updateStatus(Guid command, CommandStatus status);

    void updateExecuted(Guid id);

    void updateNotified(Guid id);

    void removeAllBeforeDate(Date cutoff);

    List<CommandEntity> getCmdEntitiesByParentCmdId(Guid parentId);

    List<Guid> getCommandIdsByEntity(Guid entityId);

    void insertCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities);

    List<CommandAssociatedEntity> getAllCommandAssociatedEntities(Guid cmdId);
}
