package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.NetworkVdsmNameMapper;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.vdsm.jsonrpc.client.BrokerCommandCallback;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetCapabilitiesAsyncVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {

    @Inject
    private NetworkVdsmNameMapper vdsmNameMapper;
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;


    public GetCapabilitiesAsyncVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            getBroker().getCapabilities(new GetCapabilitiesVDSCommandCallback());
        } catch (Throwable t) {
            getParameters().getCallback().onFailure(t);
            throw t;
        }
    }

    private class GetCapabilitiesVDSCommandCallback implements BrokerCommandCallback {

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                infoReturn = new VDSInfoReturn(response);
                proceedProxyReturnValue();
                vdsBrokerObjectsBuilder.updateVDSDynamicData(getVds(),
                        vdsmNameMapper.createVdsmNameMapping(getVds().getClusterId()),
                        infoReturn.info);
                setReturnValue(getVds());
                getParameters().getCallback().onResponse(Collections.singletonMap("result", getVDSReturnValue()));
            } catch (Exception ex) {
                getParameters().getCallback().onFailure(ex);
            }
        }

        @Override
        public void onFailure(Map<String, Object> response) {
            try {
                infoReturn = new VDSInfoReturn().withStatus(response);
                proceedProxyReturnValue();
            } catch (Exception ex) {
                getParameters().getCallback().onFailure(ex);
            }
        }
    }

    @Override
    protected String getCommandName() {
        return "Get Host Capabilities";
    }
}
