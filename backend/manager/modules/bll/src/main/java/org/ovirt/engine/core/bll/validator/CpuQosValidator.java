package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.QosDao;
import org.ovirt.engine.core.di.Injector;

public class CpuQosValidator extends QosValidator<CpuQos> {

    public CpuQosValidator(CpuQos qos) {
        super(qos);
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return Injector.get(CpuQosDao.class);
    }

    @Override
    public ValidationResult requiredValuesPresent() {
        if (getQos().getCpuLimit() == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QOS_MISSING_VALUES);
        }
        return ValidationResult.VALID;
    }
}
