package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
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
    protected void ExecuteVdsBrokerCommand() {
        infoReturn = getBroker().getVdsStats();
        ProceedProxyReturnValue();
        if (getVds().getInterfaces().isEmpty()) {
            List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(
                    getVds().getId());
            for (VdsNetworkInterface iface : interfaces) {
                getVds().getInterfaces().add(iface);
            }
        }
        VdsBrokerObjectsBuilder.updateVDSStatisticsData(getVds(), infoReturn.mInfo);
        VdsBrokerObjectsBuilder.checkTimeDrift(getVds(), infoReturn.mInfo);
    }
}
