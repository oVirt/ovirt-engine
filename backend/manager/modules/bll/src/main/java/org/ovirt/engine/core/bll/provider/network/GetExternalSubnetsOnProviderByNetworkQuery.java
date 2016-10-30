package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetExternalSubnetsOnProviderByNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    @Inject
    private ProviderDao providerDao;

    public GetExternalSubnetsOnProviderByNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Network network = networkDao.get(getParameters().getId());
        if (network == null || !network.isExternal()) {
            return;
        }

        Provider<?> provider = providerDao.get(network.getProvidedBy().getProviderId());
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
