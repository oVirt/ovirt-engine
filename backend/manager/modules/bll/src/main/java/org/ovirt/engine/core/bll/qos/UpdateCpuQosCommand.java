package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class UpdateCpuQosCommand extends UpdateQosCommandBase<CpuQos, QosValidator<CpuQos>> {

    public UpdateCpuQosCommand(QosParametersBase<CpuQos> parameters) {
        super(parameters);
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return getDbFacade().getCpuQosDao();
    }

    @Override
    protected QosValidator<CpuQos> getQosValidator(CpuQos qos) {
        return new CpuQosValidator(qos);
    }
}
