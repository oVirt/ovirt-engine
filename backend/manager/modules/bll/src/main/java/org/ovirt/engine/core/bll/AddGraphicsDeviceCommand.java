package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;

public class AddGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    protected AddGraphicsDeviceCommand(GraphicsParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        if (graphicsDev.getDeviceId() == null) {
            graphicsDev.setDeviceId(Guid.newGuid());
        }
        getVmDeviceDao().save(graphicsDev);
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }
}
