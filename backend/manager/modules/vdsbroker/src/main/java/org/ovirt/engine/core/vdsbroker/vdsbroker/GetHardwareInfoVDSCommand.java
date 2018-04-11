package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;

public class GetHardwareInfoVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    public GetHardwareInfoVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        infoReturn = getBroker().getHardwareInfo();
        proceedProxyReturnValue();
        vdsBrokerObjectsBuilder.updateHardwareSystemInformation(infoReturn.info, getVds());
    }
}
