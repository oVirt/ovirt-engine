package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;

public class UpdateGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    public UpdateGraphicsDeviceCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        getVmDeviceDao().update(graphicsDev);
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

}
