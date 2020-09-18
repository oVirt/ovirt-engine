package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;

/**
 * Send variables that set Hosted Engine maintenance mode to VDSM
 */
public class SetHaMaintenanceModeVDSCommand extends VdsBrokerCommand<SetHaMaintenanceModeVDSCommandParameters> {

    public SetHaMaintenanceModeVDSCommand(SetHaMaintenanceModeVDSCommandParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            status = getBroker().setHaMaintenanceMode(getParameters().getMode().name(),
                    getParameters().isEnabled());
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeErrorAndReport(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }
}
