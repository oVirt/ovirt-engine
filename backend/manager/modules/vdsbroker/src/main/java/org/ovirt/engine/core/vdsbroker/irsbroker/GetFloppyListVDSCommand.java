package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class GetFloppyListVDSCommand<P extends IrsBaseVDSCommandParameters> extends GetIsoListVDSCommand<P> {
    protected IsoListReturnForXmlRpc _isoList;

    public GetFloppyListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        _isoList = getIrsProxy().getFloppyList(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        if (_isoList.mVMList != null && _isoList.mVMList.length > 0) {
            setReturnValue(new java.util.ArrayList<String>(java.util.Arrays.asList(_isoList.mVMList)));
        } else {
            setReturnValue(new java.util.ArrayList<String>());
        }
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
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
