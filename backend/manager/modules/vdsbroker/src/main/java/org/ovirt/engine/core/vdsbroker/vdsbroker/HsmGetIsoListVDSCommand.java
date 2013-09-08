package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMGetIsoListParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IsoListReturnForXmlRpc;

public class HsmGetIsoListVDSCommand<P extends HSMGetIsoListParameters> extends VdsBrokerCommand<P> {
    public HsmGetIsoListVDSCommand(P parameters) {
        super(parameters);
    }

    protected IsoListReturnForXmlRpc _isoList;

    @Override
    protected void executeVdsBrokerCommand() {
        _isoList = getBroker().getIsoList(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        if (_isoList.mVMList != null && _isoList.mVMList.length > 0) {
            setReturnValue(new java.util.ArrayList<String>(java.util.Arrays.asList(_isoList.mVMList)));
        } else {
            setReturnValue(new java.util.ArrayList<String>());
        }
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _isoList;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _isoList.mStatus;
    }
}
