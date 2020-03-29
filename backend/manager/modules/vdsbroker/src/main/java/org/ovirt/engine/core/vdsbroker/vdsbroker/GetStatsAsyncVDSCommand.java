package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.vdsm.jsonrpc.client.BrokerCommandCallback;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetStatsAsyncVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    @Inject
    private MultipathHealthHandler multipathHealthHandler;

    public GetStatsAsyncVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            getBroker().getVdsStats(new GetStatsVDSCommandCallback());
        } catch (Throwable t) {
            getParameters().getCallback().onFailure(t);
            throw t;
        }
    }

    private class GetStatsVDSCommandCallback implements BrokerCommandCallback {

        @Override
        public void onResponse(Map<String, Object> response) {
            try {
                infoReturn = new VDSInfoReturn(response);
                proceedProxyReturnValue();
                vdsBrokerObjectsBuilder.updateVDSStatisticsData(getVds(), infoReturn.info);
                vdsBrokerObjectsBuilder.checkTimeDrift(getVds(), infoReturn.info);
                multipathHealthHandler.handleMultipathHealthReport(getVds(), infoReturn.info);
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
        return "Get Host Statistics";
    }
}
