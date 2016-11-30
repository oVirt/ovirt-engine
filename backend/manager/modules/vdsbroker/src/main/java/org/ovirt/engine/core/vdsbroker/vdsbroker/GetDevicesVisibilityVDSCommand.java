package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetDevicesVisibilityVDSCommandParameters;

public class GetDevicesVisibilityVDSCommand<P extends GetDevicesVisibilityVDSCommandParameters> extends VdsBrokerCommand<P> {

    private DevicesVisibilityMapReturn result;

    public GetDevicesVisibilityVDSCommand(P parameters) {
        super(parameters);
    }

    protected void executeVdsBrokerCommand() {
        result = getBroker().getDevicesVisibility(getParameters().getDevicesIds());
        proceedProxyReturnValue();
        setReturnValue(result.getDevicesVisibilityResult());
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }
}
