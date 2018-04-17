package org.ovirt.engine.core.bll.qos;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmSlaPolicyUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.CpuQosValidator;
import org.ovirt.engine.core.bll.validator.QosValidator;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.qos.QosDao;

public class RemoveCpuQosCommand extends RemoveQosCommandBase<CpuQos, QosValidator<CpuQos>> {

    @Inject
    VmSlaPolicyUtils vmSlaPolicyUtils;

    public RemoveCpuQosCommand(QosParametersBase<CpuQos> parameters, CommandContext cmdContext) {
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
        List<Guid> vmIds = vmSlaPolicyUtils.getRunningVmsWithCpuQos(getQosId());

        super.executeCommand();

        // After successful command, refresh qos

        CpuQos unlimitedQos = new CpuQos();
        unlimitedQos.setCpuLimit(100);
        vmSlaPolicyUtils.refreshVmsCpuQos(vmIds, unlimitedQos);
    }
}
