package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.errors.EngineMessage;

@InternalCommandAttribute
public class UpdateRngDeviceCommand extends AbstractRngDeviceCommand<RngDeviceParameters> {

    public UpdateRngDeviceCommand(RngDeviceParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getTemplateType() != VmEntityType.INSTANCE_TYPE && !isBlankTemplate()) {
            if (!validate(getVirtioRngValidator().canAddRngDevice(getCluster(), getParameters().getRngDevice()))) {
                return false;
            }
        }

        if (getRngDevices().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RNG_NOT_FOUND);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VmDevice rngDevice = getParameters().getRngDevice();
        getDbFacade().getVmDeviceDao().update(rngDevice);
        setSucceeded(true);
    }
}
