package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public abstract class StoragePoolManagementCommandBase<T extends StoragePoolManagementParameter> extends
        StorageHandlingCommandBase<T> {
    public StoragePoolManagementCommandBase(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected StoragePoolManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public StoragePool getStoragePool() {
        return getParameters().getStoragePool();
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__POOL);
        return super.canDoAction();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class, UpdateEntity.class);
        return super.getValidationGroups();
    }
}
