package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetAllVmStatsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VmStatsVdsBrokerCommand<P> {
    public GetAllVmStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getAllVmStats();
        proceedProxyReturnValue();
        Map<Guid, VmInternalData> returnVMs = new HashMap<>();
        for (int idx = 0; idx < vmListReturn.infoList.length; ++idx) {
            VmInternalData vmInternalData = createVmInternalData(vmListReturn.infoList[idx]);
            returnVMs.put(vmInternalData.getVmDynamic().getId(), vmInternalData);
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
