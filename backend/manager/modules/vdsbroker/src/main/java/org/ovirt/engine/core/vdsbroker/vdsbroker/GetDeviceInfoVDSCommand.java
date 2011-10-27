package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class GetDeviceInfoVDSCommand<P extends GetDeviceInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneLUNReturnForXmlRpc _result;

    public GetDeviceInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getDeviceInfo(getParameters().getLUNID());
        ProceedProxyReturnValue();
        setReturnValue(GetDeviceListVDSCommand.ParseLunFromXmlRpc(_result.lunInfo));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
