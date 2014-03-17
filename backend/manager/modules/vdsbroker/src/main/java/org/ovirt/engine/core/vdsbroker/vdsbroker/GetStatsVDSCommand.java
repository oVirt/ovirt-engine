package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
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
        if (getVds().getInterfaces().isEmpty()) {
            List<VdsNetworkInterface> nics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getVds().getId());
            getVds().getInterfaces().addAll(nics);
        }

        VdsBrokerObjectsBuilder.updateVDSStatisticsData(getVds(), infoReturn.mInfo);
        VdsBrokerObjectsBuilder.checkTimeDrift(getVds(), infoReturn.mInfo);
    }
}
