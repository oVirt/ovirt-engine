package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

public abstract class StoragePoolManagementCommandBase<T extends StoragePoolManagementParameter> extends
        StorageHandlingCommandBase<T> {

    @Inject
    private StoragePoolDao storagePoolDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected StoragePoolManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    public StoragePoolManagementCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public StoragePool getStoragePool() {
        return getParameters().getStoragePool();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__POOL);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class, UpdateEntity.class);
        return super.getValidationGroups();
    }

    protected boolean isStoragePoolUnique(String storagePoolName) {
        List<StoragePool> sps = storagePoolDao.getByName(storagePoolName, true);
        return sps == null || sps.isEmpty();
    }

}
