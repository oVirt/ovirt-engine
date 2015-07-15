package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetCapabilitiesVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    public GetCapabilitiesVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        infoReturn = getBroker().getCapabilities();
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVDSDynamicData(getVds(), infoReturn.info);
        setReturnValue(getVds());
    }
}
