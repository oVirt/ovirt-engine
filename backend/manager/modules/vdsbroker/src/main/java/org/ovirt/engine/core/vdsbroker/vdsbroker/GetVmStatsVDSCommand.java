package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetVmStatsVDSCommand<P extends GetVmStatsVDSCommandParameters> extends VmStatsVdsBrokerCommand<P> {
    public GetVmStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getVmStats(getParameters().getVmId().toString());
        proceedProxyReturnValue();
        setReturnValue(createVmInternalData(vmListReturn.infoList[0]));
    }
}
