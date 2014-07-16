package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@InternalCommandAttribute
public class UpdateRngDeviceCommand extends AbstractRngDeviceCommand<RngDeviceParameters> {

    public UpdateRngDeviceCommand(RngDeviceParameters parameters) {
        this(parameters, null);
    }

    public UpdateRngDeviceCommand(RngDeviceParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (!isRngSupportedByCluster() && getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }

        if (getRngDevices().isEmpty()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_RNG_NOT_FOUND);
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
