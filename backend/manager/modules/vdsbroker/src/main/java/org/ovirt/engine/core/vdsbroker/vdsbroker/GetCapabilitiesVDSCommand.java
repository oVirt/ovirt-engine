package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.NetworkVdsmNameMapper;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetCapabilitiesVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {

    @Inject
    private NetworkVdsmNameMapper vdsmNameMapper;
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    public GetCapabilitiesVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        infoReturn = getBroker().getCapabilities();
        proceedProxyReturnValue();
        vdsBrokerObjectsBuilder.updateVDSDynamicData(
                getVds(), vdsmNameMapper.createVdsmNameMapping(getVds().getClusterId()), infoReturn.info);
        setReturnValue(getVds());
    }
}
