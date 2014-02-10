package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmDevice;

public class UpdateGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    protected UpdateGraphicsDeviceCommand(GraphicsParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        getVmDeviceDao().update(graphicsDev);
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

}
