package org.ovirt.engine.core.bll;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.EngineMessage;

@InternalCommandAttribute
public class RemoveRngDeviceCommand extends AbstractRngDeviceCommand<RngDeviceParameters> {

    public RemoveRngDeviceCommand(RngDeviceParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getRngDevices().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_RNG_NOT_FOUND);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        List<VmDevice> rngDevices = getRngDevices();
        Set<VmDeviceId> idsToRemove = new HashSet<>();

        for (VmDevice dev : rngDevices) {
            idsToRemove.add(dev.getId());
        }

        getDbFacade().getVmDeviceDao().removeAll(idsToRemove);
        setSucceeded(true);
    }
}
