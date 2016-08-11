package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetAllVmStatsVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VmStatsVdsBrokerCommand<P> {

    public GetAllVmStatsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getAllVmStats();
        proceedProxyReturnValue();
        Map<Guid, VdsmVm> returnVMs = Arrays.stream(vmListReturn.infoList)
                .map(this::createVdsmVm)
                .collect(Collectors.toMap(vm -> vm.getVmDynamic().getId(), vm -> vm));
        setReturnValue(returnVMs);
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
