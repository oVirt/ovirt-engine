package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDAO;

public abstract class StoragePoolManagementCommandBase<T extends StoragePoolManagementParameter> extends
        StorageHandlingCommandBase<T> {
    public StoragePoolManagementCommandBase(T parameters) {
        super(parameters, null);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
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
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__POOL);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class, UpdateEntity.class);
        return super.getValidationGroups();
    }

    protected boolean isStoragePoolUnique(String storagePoolName) {
        StoragePoolDAO spDao = getStoragePoolDAO();
        List<StoragePool> sps = spDao.getByName(storagePoolName, false);
        return (sps == null || sps.isEmpty());
    }
}
