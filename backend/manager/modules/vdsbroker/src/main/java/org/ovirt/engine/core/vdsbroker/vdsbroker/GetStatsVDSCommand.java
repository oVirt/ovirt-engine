package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetStatsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    @Inject
    private MultipathHealthHandler multipathHealthHandler;
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    public GetStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        infoReturn = getBroker().getVdsStats();
        proceedProxyReturnValue();

        vdsBrokerObjectsBuilder.updateVDSStatisticsData(getVds(), infoReturn.info);
        multipathHealthHandler.handleMultipathHealthReport(getVds(), infoReturn.info);
        vdsBrokerObjectsBuilder.checkTimeDrift(getVds(), infoReturn.info);
    }
}
