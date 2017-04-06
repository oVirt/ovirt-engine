package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class UpdateGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public UpdateGraphicsDeviceCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        vmDeviceDao.update(graphicsDev);
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

}
