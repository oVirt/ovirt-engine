package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    public RemoveWatchdogCommand(WatchdogParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    @Override
    protected void executeCommand() {
        // there can be only one, but this way it is easier
        for (VmDevice watchdog : getWatchdogs()) {
            getVmDeviceDao().remove(watchdog.getId());
        }
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (getWatchdogs().isEmpty()) {
            return failValidation(EngineMessage.WATCHDOG_NOT_FOUND);
        }
        return true;
    }

}
