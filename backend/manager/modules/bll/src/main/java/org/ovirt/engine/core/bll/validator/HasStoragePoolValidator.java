package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.HasStoragePool;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.di.Injector;

/**
 * A validator for an {@link HasStoragePool} instance.
 */
public class HasStoragePoolValidator {
    private HasStoragePool entity;
    private StoragePoolValidator spValidator;

    public HasStoragePoolValidator(HasStoragePool entity) {
        this.entity = entity;
    }

    /**
     * @return An error iff the data center to which the network belongs doesn't exist.
     */
    public ValidationResult storagePoolExists() {
        if (entity.getStoragePoolId() == null) {
            return ValidationResult.VALID;
        }
        return getStoragePoolValidator().exists();
    }

    private StoragePoolValidator getStoragePoolValidator() {
        if (spValidator == null) {
            spValidator = new StoragePoolValidator(getStoragePoolDao().get(entity.getStoragePoolId()));
        }
        return spValidator;
    }

    protected StoragePoolDao getStoragePoolDao() {
        return Injector.get(StoragePoolDao.class);
    }

}
