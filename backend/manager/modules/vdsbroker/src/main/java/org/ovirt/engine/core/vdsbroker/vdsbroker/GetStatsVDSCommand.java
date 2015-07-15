package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetStatsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
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

        VdsBrokerObjectsBuilder.updateVDSStatisticsData(getVds(), infoReturn.info);
        VdsBrokerObjectsBuilder.checkTimeDrift(getVds(), infoReturn.info);
    }
}
