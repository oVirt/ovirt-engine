package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.qos.QosDao;

public class CpuQosValidator extends QosValidator<CpuQos> {

    public CpuQosValidator(CpuQos qos) {
        super(qos);
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return DbFacade.getInstance().getCpuQosDao();
    }

    @Override
    public ValidationResult allValuesPresent() {
        if (getQos().getCpuLimit() == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_QOS_MISSING_VALUES);
        }
        return ValidationResult.VALID;
    }
}
