package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    public RemoveWatchdogCommand(WatchdogParameters parameters) {
        this(parameters, null);
    }

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

    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        if (getWatchdogs().isEmpty()) {
            return failCanDoAction(VdcBllMessages.WATCHDOG_NOT_FOUND);
        }
        return true;
    }

}
