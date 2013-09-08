package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;

public class GetHardwareInfoVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    public GetHardwareInfoVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        infoReturn = getBroker().getHardwareInfo();
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.UpdateHardwareSystemInformation(infoReturn.mInfo, getVds());
    }
}
