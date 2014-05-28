package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.qos.QosDao;

public class StorageQosValidator extends QosValidator<StorageQos> {

    public StorageQosValidator(StorageQos qos) {
        super(qos);
    }

    @Override
    protected QosDao<StorageQos> getQosDao() {
        return DbFacade.getInstance().getStorageQosDao();
    }

    /*
     * Bytes and iops values are independent categories.
     * Setting one value leads to reseting the other two in the same
     * category to unlimited.
     * A non-zero total value cannot be used with non-zero read or
     * write value.
     */
    @Override
    public ValidationResult allValuesPresent() {
        if (missingCategoryValues(getQos().getMaxThroughput(),
                getQos().getMaxReadThroughput(),
                getQos().getMaxWriteThroughput())
                || missingCategoryValues(getQos().getMaxIops(),
                        getQos().getMaxReadIops(),
                        getQos().getMaxWriteIops())) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_QOS_MISSING_VALUES);
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
