package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    protected RemoveGraphicsDeviceCommand(GraphicsParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        getVmDeviceDao().remove(graphicsDev.getId());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        GraphicsDevice dev = getParameters().getDev();

        if (dev.getDeviceId() == null || dev.getVmId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_REMOVE_GRAPHICS_DEV_INVALID_PARAMS);
        }

        return true;
    }

}
