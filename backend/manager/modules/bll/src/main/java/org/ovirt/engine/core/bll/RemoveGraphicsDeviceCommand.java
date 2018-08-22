package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class RemoveGraphicsDeviceCommand extends AbstractGraphicsDeviceCommand<GraphicsParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public RemoveGraphicsDeviceCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDeviceId graphicsDevId = getParameters().getDev().getId();

        CompensationUtils.removeEntity(graphicsDevId, vmDeviceDao, getCompensationContextIfEnabledByCaller());
        compensationStateChanged();

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
