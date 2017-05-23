package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
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

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetExternalSubnetsOnProviderByNetworkQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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

        NetworkProviderProxy client = providerProxyFactory.create(provider);
        getQueryReturnValue().setReturnValue(client.getAllSubnets(network.getProvidedBy()));
    }
}
