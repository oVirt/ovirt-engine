package org.ovirt.engine.core.bll.qos;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.dao.qos.QosDao;

public class UpdateCpuQosCommand extends UpdateQosCommandBase<CpuQos, QosValidator<CpuQos>> {

    @Inject
    VmSlaPolicyUtils vmSlaPolicyUtils;

    public UpdateCpuQosCommand(QosParametersBase<CpuQos> parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected QosDao<CpuQos> getQosDao() {
        return cpuQosDao;
    }

    @Override
    protected QosValidator<CpuQos> getQosValidator(CpuQos qos) {
        return new CpuQosValidator(qos);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();

        // Refresh VM sla policy
        vmSlaPolicyUtils.refreshRunningVmsWithCpuQos(getQosId(), getQos());
    }
}
