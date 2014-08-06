package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class RemoveCpuQosCommand extends RemoveQosCommandBase<CpuQos, QosValidator<CpuQos>> {

    public RemoveCpuQosCommand(QosParametersBase<CpuQos> parameters) {
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
