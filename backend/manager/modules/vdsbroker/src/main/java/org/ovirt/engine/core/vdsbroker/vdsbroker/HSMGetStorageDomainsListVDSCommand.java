package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class HSMGetStorageDomainsListVDSCommand<P extends HSMGetStorageDomainsListVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private StorageDomainListReturnForXmlRpc _result;

    public HSMGetStorageDomainsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getStorageDomainsList(getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainType().getValue(), getParameters().getStorageType().getValue(),
                ((getParameters().getPath()) != null) ? getParameters().getPath() : "");
        ProceedProxyReturnValue();

        java.util.ArrayList<Guid> domains = new java.util.ArrayList<Guid>();
        for (String domain : _result.mStorageDomainList) {
            domains.add(new Guid(domain));
        }
        setReturnValue(domains);
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
