package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.CompensationUtils;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;

@InternalCommandAttribute
public class RemoveRngDeviceCommand extends AbstractRngDeviceCommand<RngDeviceParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

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

        CompensationUtils.removeEntities(rngDevices, vmDeviceDao, getCompensationContextIfEnabledByCaller());
        compensationStateChanged();

        setSucceeded(true);
    }
}
