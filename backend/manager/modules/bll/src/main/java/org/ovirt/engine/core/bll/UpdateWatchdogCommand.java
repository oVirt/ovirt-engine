package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmDeviceDao;

@ValidateSupportsTransaction
public class UpdateWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    @Inject
    private VmDeviceDao vmDeviceDao;

    public UpdateWatchdogCommand(WatchdogParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        List<VmDevice> watchdogs =
                getWatchdogs();
        VmDevice watchdogDevice = watchdogs.get(0); // there must be only one
        watchdogDevice.setSpecParams(getSpecParams());
        vmDeviceDao.update(watchdogDevice);
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        List<VmDevice> watchdogs = getWatchdogs();
        if (watchdogs.isEmpty()) {
            return failValidation(EngineMessage.WATCHDOG_NOT_FOUND);
        }

        if (!validate(validateWatchdog())) {
            return false;
        }

        return true;
    }

}
