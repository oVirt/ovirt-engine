package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class UpdateWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    public UpdateWatchdogCommand(WatchdogParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        List<VmDevice> watchdogs =
                getWatchdogs();
        VmDevice watchdogDevice = watchdogs.get(0); // there must be only one
        watchdogDevice.setSpecParams(getSpecParams());
        getVmDeviceDao().update(watchdogDevice);
        setSucceeded(true);
    }

    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        List<VmDevice> watchdogs = getWatchdogs();
        if (watchdogs.isEmpty()) {
            return failCanDoAction(VdcBllMessages.WATCHDOG_NOT_FOUND);
        }
        return true;
    }

}
