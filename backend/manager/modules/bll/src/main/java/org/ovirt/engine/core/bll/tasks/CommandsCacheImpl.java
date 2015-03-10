package org.ovirt.engine.core.bll.tasks;

import java.util.List;
import java.util.Set;

import javax.transaction.Transaction;

import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class CommandsCacheImpl implements CommandsCache {

    CacheWrapper<Guid, CommandEntity> commandMap;
    private volatile boolean cacheInitialized;
    private Object LOCK = new Object();

    public CommandsCacheImpl() {
        commandMap = CacheProviderFactory.<Guid, CommandEntity> getCacheWrapper();
    }

    private void initializeCache() {
        if (!cacheInitialized) {
            synchronized(LOCK) {
                if (!cacheInitialized) {
                    List<CommandEntity> cmdEntities = DbFacade.getInstance().getCommandEntityDao().getAll();
                    for (CommandEntity cmdEntity : cmdEntities) {
                        commandMap.put(cmdEntity.getId(), cmdEntity);
                    }
                    cacheInitialized = true;
                }
            }
        }
    }

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
        DbFacade.getInstance().getCommandEntityDao().remove(commandId);
    }

    @Override
    public void put(final CommandEntity cmdEntity) {
        commandMap.put(cmdEntity.getId(), cmdEntity);
        saveOrUpdateWithoutTransaction(cmdEntity);
    }

    public void removeAllCommandsBeforeDate(DateTime cutoff) {
        DbFacade.getInstance().getCommandEntityDao().removeAllBeforeDate(cutoff);
        cacheInitialized = false;
        initializeCache();
    }

    public void updateCommandStatus(Guid commandId, CommandStatus status) {
        final CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCommandStatus(status);
            saveOrUpdateWithoutTransaction(cmdEntity);
        }
    }

    public void updateCommandExecuted(final Guid commandId) {
        CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setExecuted(true);
            DbFacade.getInstance().getCommandEntityDao().updateExecuted(commandId);
        }
    }

    public void saveOrUpdateWithoutTransaction(CommandEntity cmdEntity) {
        Transaction transaction = TransactionSupport.suspend();
        try {
            DbFacade.getInstance().getCommandEntityDao().saveOrUpdate(cmdEntity);
        } finally {
            if (transaction != null) {
                TransactionSupport.resume(transaction);
            }
        }
    }

    public void updateCallBackNotified(final Guid commandId) {
        CommandEntity cmdEntity = get(commandId);
        if (cmdEntity != null) {
            cmdEntity.setCallBackNotified(true);
            DbFacade.getInstance().getCommandEntityDao().updateNotified(commandId);
        }
    }

    @Override
    public List<CommandEntity> getChildCmdsByParentCmdId(Guid cmdId) {
        return DbFacade.getInstance().getCommandEntityDao().getCmdEntitiesByParentCmdId(cmdId);
    }
}
