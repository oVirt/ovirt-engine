package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetImageDomainsListVDSCommand<P extends GetImageDomainsListVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StorageDomainGuidListReturnForXmlRpc _result;

    public GetImageDomainsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        _result = getIrsProxy().getImageDomainsList(getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString());
        proceedProxyReturnValue();
        java.util.ArrayList<Guid> tempRetValue = new java.util.ArrayList<Guid>(_result.mStorageDomainGuidList.length);
        for (int i = 0; i < _result.mStorageDomainGuidList.length; i++) {
            tempRetValue.add(new Guid(_result.mStorageDomainGuidList[i]));
        }
        setReturnValue(tempRetValue);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    @Override
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case GetStorageDomainListError:
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().mMessage));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.proceedProxyReturnValue();
            break;
        }
    }
}
