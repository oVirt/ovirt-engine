package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class HSMGetStorageDomainsListVDSCommand<P extends HSMGetStorageDomainsListVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private StorageDomainListReturnForXmlRpc _result;

    public HSMGetStorageDomainsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result =
                getBroker().getStorageDomainsList(getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainType().getValue(),
                        getParameters().getStorageType() != null ? Integer.valueOf(getParameters().getStorageType()
                                .getValue()).toString() : "",
                        ((getParameters().getPath()) != null) ? getParameters().getPath() : "");
        proceedProxyReturnValue();

        ArrayList<Guid> domains = new ArrayList<Guid>();
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
