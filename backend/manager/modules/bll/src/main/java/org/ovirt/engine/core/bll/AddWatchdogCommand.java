package org.ovirt.engine.core.bll;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class AddWatchdogCommand extends AbstractVmWatchdogCommand<WatchdogParameters> {

    public AddWatchdogCommand(WatchdogParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public AddWatchdogCommand(WatchdogParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmDevice watchdogDevice = new VmDevice();
        watchdogDevice.setId(new VmDeviceId(Guid.Empty, getParameters().getId()));
        watchdogDevice.setDevice(VmDeviceType.WATCHDOG.getName());
        watchdogDevice.setType(VmDeviceGeneralType.WATCHDOG);
        watchdogDevice.setAddress(StringUtils.EMPTY);
        watchdogDevice.setSpecParams(getSpecParams());
        getVmDeviceDao().save(watchdogDevice);
        setSucceeded(true);
        setActionReturnValue(watchdogDevice.getId().getDeviceId());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        VdcQueryReturnValue returnValue =
                runInternalQuery(VdcQueryType.GetWatchdog,
                        new IdQueryParameters(getParameters().getId()));
        Collection<VmWatchdog> watchdogs = returnValue.getReturnValue();
        if (!watchdogs.isEmpty()) {
            return failValidation(EngineMessage.WATCHDOG_ALREADY_EXISTS);
        }

        if (!validate(validateWatchdog())) {
            return false;
        }

        return true;
    }

}
