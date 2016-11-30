package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetImageTransferSessionStatsVDSCommandParameters;


public class GetImageTransferSessionStatsVDSCommand<P extends GetImageTransferSessionStatsVDSCommandParameters> extends VdsBrokerCommand<P> {
    OneMapReturn retval;

    public GetImageTransferSessionStatsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        retval = getBroker().get_image_transfer_session_stats(
                getParameters().getTicketId().toString());

        proceedProxyReturnValue();
        setReturnValue(retval.getResultMap());
    }

    @Override
    protected Status getReturnStatus() {
        return retval.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return retval.getResultMap();
    }
}
