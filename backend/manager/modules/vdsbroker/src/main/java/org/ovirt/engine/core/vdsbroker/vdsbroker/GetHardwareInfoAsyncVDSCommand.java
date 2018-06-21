package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.vdsm.jsonrpc.client.BrokerCommandCallback;

public class GetHardwareInfoAsyncVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    public GetHardwareInfoAsyncVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            getBroker().getHardwareInfo(new GetHardwareInfoVDSCommandCallback());
        } catch (Throwable t) {
            getParameters().getCallback().onFailure(t);
            throw t;
        }
    }

    private class GetHardwareInfoVDSCommandCallback implements BrokerCommandCallback {

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                infoReturn = new VDSInfoReturn(response);
                proceedProxyReturnValue();
                vdsBrokerObjectsBuilder.updateHardwareSystemInformation(infoReturn.info, getVds());
                if (getParameters().getCallback() != null) {
                    getParameters().getCallback().onResponse(Collections.singletonMap("result", getVDSReturnValue()));
                }
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
        return "Get Host Hardware Info";
    }
}
