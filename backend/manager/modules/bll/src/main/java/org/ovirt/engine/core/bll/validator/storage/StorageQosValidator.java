package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.di.Injector;

public class StorageQosValidator extends QosValidator<StorageQos> {

    public StorageQosValidator(StorageQos qos) {
        super(qos);
    }

    @Override
    protected QosDao<StorageQos> getQosDao() {
        return Injector.get(StorageQosDao.class);
    }

    /*
     * Bytes and iops values are independent categories.
     * Setting one value leads to reseting the other two in the same
     * category to unlimited.
     * A non-zero total value cannot be used with non-zero read or
     * write value.
     */
    @Override
    public ValidationResult requiredValuesPresent() {
        if (missingCategoryValues(getQos().getMaxThroughput(),
                getQos().getMaxReadThroughput(),
                getQos().getMaxWriteThroughput())
                || missingCategoryValues(getQos().getMaxIops(),
                        getQos().getMaxReadIops(),
                        getQos().getMaxWriteIops())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_QOS_ILLEGAL_VALUES);
        }
        return ValidationResult.VALID;
    }

    private boolean missingCategoryValues(Integer total, Integer read, Integer write) {
        return isPositive(total) && (isPositive(read) || isPositive(write));
    }

    private boolean isPositive(Integer value) {
        return value != null && value > 0;
    }
}
