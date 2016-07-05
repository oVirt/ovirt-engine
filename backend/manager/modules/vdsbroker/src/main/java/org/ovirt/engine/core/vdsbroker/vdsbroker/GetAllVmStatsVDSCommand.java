package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VdsmVm;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetAllVmStatsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VmStatsVdsBrokerCommand<P> {
    public GetAllVmStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getAllVmStats();
        proceedProxyReturnValue();
        Map<Guid, VdsmVm> returnVMs = new HashMap<>();
        for (int idx = 0; idx < vmListReturn.infoList.length; ++idx) {
            VdsmVm vmInternalData = createVdsmVm(vmListReturn.infoList[idx]);
            returnVMs.put(vmInternalData.getVmDynamic().getId(), vmInternalData);
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
