package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A validator for an {@link HasStoragePool} instance.
 */
public class HasStoragePoolValidator {
    private HasStoragePool entity;
    private StoragePool storagePool;

    public HasStoragePoolValidator(HasStoragePool entity) {
        this.entity = entity;
    }

    /**
     * @return An error iff the data center to which the network belongs doesn't exist.
     */
    public ValidationResult storagePoolExists() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST)
                .when(entity.getStoragePoolId() != null && getStoragePool() == null);
    }

    private StoragePool getStoragePool() {
        if (storagePool == null) {
            storagePool = getStoragePoolDao().get(entity.getStoragePoolId());
        }
        return storagePool;
    }

    protected StoragePoolDao getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

}
