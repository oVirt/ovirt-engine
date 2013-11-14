package org.ovirt.engine.core.bll.provider.network;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetExternalSubnetsOnProviderByNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetExternalSubnetsOnProviderByNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Network network = getDbFacade().getNetworkDao().get(getParameters().getId());
        if (network == null || !network.isExternal()) {
            return;
        }

        Provider<?> provider = getDbFacade().getProviderDao().get(network.getProvidedBy().getProviderId());
        if (provider == null) {
            return;
        }

        NetworkProviderProxy client = getProviderProxyFactory().create(provider);
        getQueryReturnValue().setReturnValue(client.getAllSubnets(network.getProvidedBy()));
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }
}
