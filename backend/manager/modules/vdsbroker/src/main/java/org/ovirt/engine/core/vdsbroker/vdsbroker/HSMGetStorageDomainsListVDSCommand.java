package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;

import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class HSMGetStorageDomainsListVDSCommand<P extends HSMGetStorageDomainsListVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    private StorageDomainListReturn result;

    public HSMGetStorageDomainsListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result =
                getBroker().getStorageDomainsList(getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainType().getValue(),
                        getParameters().getStorageType() != null ? Integer.toString(getParameters().getStorageType()
                                .getValue()) : "",
                        (getParameters().getPath() != null) ? getParameters().getPath() : "");
        proceedProxyReturnValue();

        ArrayList<Guid> domains = new ArrayList<>();
        for (String domain : result.storageDomainList) {
            domains.add(new Guid(domain));
        }
        setReturnValue(domains);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
