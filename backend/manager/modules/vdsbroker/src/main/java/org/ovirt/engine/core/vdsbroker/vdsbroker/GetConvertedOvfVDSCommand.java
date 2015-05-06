package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class GetConvertedOvfVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    private OvfReturnForXmlRpc ovfReturn;

    public GetConvertedOvfVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        ovfReturn = getBroker().getConvertedVm(getParameters().getVmId().toString());
        proceedProxyReturnValue();
        setReturnValue(ovfReturn.ovf);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return ovfReturn.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return ovfReturn;
    }
}

