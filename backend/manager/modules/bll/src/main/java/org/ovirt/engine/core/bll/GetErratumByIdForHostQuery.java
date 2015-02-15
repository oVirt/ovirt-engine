package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;

public class GetErratumByIdForHostQuery<P extends HostErratumQueryParameters> extends QueriesCommandBase<P> {

    public GetErratumByIdForHostQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = getDbFacade().getVdsStaticDao().get(getParameters().getId());
        if (host == null || host.getHostProviderId() == null) {
            return;
        }

        Provider<?> provider = getDbFacade().getProviderDao().get(host.getHostProviderId());
        if (provider != null) {
            HostProviderProxy proxy = (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
            getQueryReturnValue().setReturnValue(proxy.getErratumForHost(host.getHostName(),
                    getParameters().getErratumId()));
        }
    }
}
