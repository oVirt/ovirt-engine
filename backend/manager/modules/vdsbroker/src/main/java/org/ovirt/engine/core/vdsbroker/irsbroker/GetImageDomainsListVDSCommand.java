package org.ovirt.engine.core.vdsbroker.irsbroker;

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
        ProceedProxyReturnValue();
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
}
