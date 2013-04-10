package org.ovirt.engine.core.bll;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    public AddWatchdogCommand(WatchdogParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice watchdogDevice = new VmDevice();
        watchdogDevice.setId(new VmDeviceId(new Guid(), getParameters().getId()));
        watchdogDevice.setDevice(VmDeviceType.WATCHDOG.getName());
        watchdogDevice.setType(VmDeviceType.WATCHDOG.getName());
        watchdogDevice.setAddress(StringUtils.EMPTY);
        watchdogDevice.setSpecParams(getSpecParams());
        getVmDeviceDao().save(watchdogDevice);
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        if (getParameters().getAction() == null) {
            return failCanDoAction(VdcBllMessages.WATCHDOG_ACTION_REQUIRED);
        }
        if (getParameters().getModel() == null) {
            return failCanDoAction(VdcBllMessages.WATCHDOG_MODEL_REQUIRED);
        }
        VdcQueryReturnValue returnValue =
                getBackend().runInternalQuery(VdcQueryType.GetWatchdog,
                        new IdQueryParameters(getParameters().getId()));
        @SuppressWarnings("unchecked")
        Collection<VmWatchdog> watchdogs = (Collection<VmWatchdog>) returnValue.getReturnValue();
        if (!watchdogs.isEmpty()) {
            return failCanDoAction(VdcBllMessages.WATCHDOG_ALREADY_EXISTS);
        }
        return true;
    }

}
