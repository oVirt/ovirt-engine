package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.SetHaMaintenanceModeVDSCommandParameters;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Send variables that set Hosted Engine maintenance mode to VDSM
 */
public class SetHaMaintenanceModeVDSCommand extends VdsBrokerCommand<SetHaMaintenanceModeVDSCommandParameters> {
    @Inject
    private VdsDao vdsDao;

    public SetHaMaintenanceModeVDSCommand(SetHaMaintenanceModeVDSCommandParameters parameters) {
        super(parameters);
    }

    @PostConstruct
    public void init() {
        setVdsAndVdsStatic(vdsDao.get(getParameters().getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (getVds().getHighlyAvailableIsConfigured()) {
            try {
                status = getBroker().setHaMaintenanceMode(getParameters().getMode().name(), getParameters().isEnabled());
                proceedProxyReturnValue();
            } catch (RuntimeException e) {
                setVdsRuntimeErrorAndReport(e);
                // prevent exception handler from rethrowing an exception
                getVDSReturnValue().setExceptionString(null);
            }
        }
    }
}
