package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class CreateVGVDSCommand<P extends CreateVGVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneUuidReturnForXmlRpc _result;

    public CreateVGVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().createVG(getParameters().getStorageDomainId().toString(),
                getParameters().getDeviceList().toArray(new String[] {}));
        ProceedProxyReturnValue();
        setReturnValue(_result.mUuid);
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
