package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.dao.qos.CpuQosDao;

public class AddCpuQosCommand extends AddQosCommand<CpuQos, QosValidator<CpuQos>, CpuQosDao> {

    public AddCpuQosCommand(QosParametersBase<CpuQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected QosValidator<CpuQos> getQosValidator(CpuQos qos) {
        return new CpuQosValidator(qos);
    }

}
