package org.ovirt.engine.core.bll.provider.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetAllExternalNetworksOnProviderQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao providerDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetAllExternalNetworksOnProviderQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = providerDao.get(getParameters().getId());
        if (provider == null) {
            return;
        }

        NetworkProviderProxy client = providerProxyFactory.create(provider);
        List<Network> externalNetworks = client.getAll();

        Map<Network, Set<Guid>> externalNetworkToDcId = new HashMap<>();
        for (Network network : externalNetworks) {
            List<Guid> dcIds = storagePoolDao.getDcIdByExternalNetworkId(network.getProvidedBy().getExternalId());
            externalNetworkToDcId.put(network, new HashSet<>(dcIds));
        }

        getQueryReturnValue().setReturnValue(externalNetworkToDcId);
    }
}
