package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AddWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public AddWatchdogCommand(WatchdogParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice watchdogDevice = vmDeviceUtils.addWatchdogDevice(getParameters().getId(), getSpecParams());
        setSucceeded(true);
        setActionReturnValue(watchdogDevice.getId().getDeviceId());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        List<VmDevice> vmWatchdogDevices = vmDeviceUtils.getWatchdogs(getParameters().getId());
        if (vmWatchdogDevices != null && !vmWatchdogDevices.isEmpty()) {
            return failValidation(EngineMessage.WATCHDOG_ALREADY_EXISTS);
        }

        if (!validate(validateWatchdog())) {
            return false;
        }

        return true;
    }

}
