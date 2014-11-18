package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Send variables that set Hosted Engine maintenance mode to VDSM
 */
public class SetHaMaintenanceModeVDSCommand extends VdsBrokerCommand<SetHaMaintenanceModeVDSCommandParameters> {

    public SetHaMaintenanceModeVDSCommand(SetHaMaintenanceModeVDSCommandParameters parameters) {
        super(parameters, DbFacade.getInstance().getVdsDao().get(parameters.getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (getVds().getHighlyAvailableIsConfigured()) {
            try {
                status = getBroker().setHaMaintenanceMode(getParameters().getMode().name(), getParameters().isEnabled());
                proceedProxyReturnValue();
            }
            catch (RuntimeException e) {
                setVdsRuntimeError(e);
                // prevent exception handler from rethrowing an exception
                getVDSReturnValue().setExceptionString(null);
            }
        }
    }
}
