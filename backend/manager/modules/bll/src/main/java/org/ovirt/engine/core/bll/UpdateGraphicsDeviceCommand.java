package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class UpdateGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmHandler vmHandler;

    public UpdateGraphicsDeviceCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();

        CompensationUtils.updateEntity(graphicsDev, vmDeviceDao, getCompensationContextIfEnabledByCaller());
        compensationStateChanged();

        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (!validate(vmHandler.isGraphicsAndDisplaySupported(
                getParameters().isVm() ? getVm().getStaticData() : getVmTemplate(),
                Set.of(getParameters().getDev().getGraphicsType()),
                getCluster()))) {
            return false;
        }
        return true;
    }

}
