package org.ovirt.engine.core.bll.tasks;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.businessentities.CommandAssociatedEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CommandEntityDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Singleton
public class CommandsCacheImpl implements CommandsCache {

    private Map<Guid, CommandEntity> commandMap;
    private volatile boolean cacheInitialized;
    private Object LOCK = new Object();

    @Inject
    private CommandEntityDao commandEntityDao;

    public CommandsCacheImpl() {
        commandMap = new HashMap<>();
    }

    private void initializeCache() {
        if (!cacheInitialized) {
            synchronized(LOCK) {
                if (!cacheInitialized) {
                    List<CommandEntity> cmdEntities = commandEntityDao.getAll();
                    for (CommandEntity cmdEntity : cmdEntities) {
                        commandMap.put(cmdEntity.getId(), cmdEntity);
                    }
                    cacheInitialized = true;
                }
            }
        }
    }

    @Override
    public Set<Guid> keySet() {
        initializeCache();
        return commandMap.keySet();
    }

    @Override
    public CommandEntity get(Guid commandId) {
        initializeCache();
        return commandMap.get(commandId);
    }

    @Override
    public void remove(final Guid commandId) {
        commandMap.remove(commandId);
        commandEntityDao.remove(commandId);
    }

    @Override
    public void put(final CommandEntity cmdEntity) {
        commandMap.put(cmdEntity.getId(), cmdEntity);
        saveOrUpdateWithoutTransaction(cmdEntity);
    }

    @Override
    public void removeAllCommandsBeforeDate(DateTime cutoff) {
        commandEntityDao.removeAllBeforeDate(cutoff);
        cacheInitialized = false;
        initializeCache();
    }

    @Override
    public void updateCommandStatus(Guid commandId, CommandStatus status) {
        final CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCommandStatus(status);
            saveOrUpdateWithoutTransaction(cmdEntity);
        }
    }

    @Override
    public void updateCommandData(Guid commandId, Map<String, Serializable> data) {
        final CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setData(data);
            saveOrUpdateWithoutTransaction(cmdEntity);
        }
    }

    @Override
    public void updateCommandExecuted(final Guid commandId) {
        CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setExecuted(true);
            commandEntityDao.updateExecuted(commandId);
        }
    }

    public void saveOrUpdateWithoutTransaction(CommandEntity cmdEntity) {
        TransactionSupport.executeInSuppressed(() -> {
            commandEntityDao.saveOrUpdate(cmdEntity);
            return null;
        });
    }

    @Override
    public void updateCallbackNotified(final Guid commandId) {
        CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCallbackNotified(true);
            commandEntityDao.updateNotified(commandId);
        }
    }

    @Override
    public void persistCommandAssociatedEntities(Collection<CommandAssociatedEntity> cmdAssociatedEntities) {
        if (CollectionUtils.isEmpty(cmdAssociatedEntities)) {
            return;
        }

        TransactionSupport.executeInSuppressed(() -> {
            commandEntityDao.insertCommandAssociatedEntities(cmdAssociatedEntities);
            return null;
        });
    }

    @Override
    public List<CommandAssociatedEntity> getCommandAssociatedEntities(Guid cmdId) {
        return commandEntityDao.getAllCommandAssociatedEntities(cmdId);
    }

    @Override
    public List<Guid> getCommandIdsByEntityId(Guid entityId) {
        return commandEntityDao.getCommandIdsByEntity(entityId);
    }

    @Override
    public List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId) {
        return commandEntityDao.getCmdEntitiesByParentCmdId(cmdId);
    }
}
