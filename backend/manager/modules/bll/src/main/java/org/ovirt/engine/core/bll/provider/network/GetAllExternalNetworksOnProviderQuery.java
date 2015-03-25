package org.ovirt.engine.core.bll.provider.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllExternalNetworksOnProviderQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllExternalNetworksOnProviderQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = getDbFacade().getProviderDao().get(getParameters().getId());
        if (provider == null) {
            return;
        }

        NetworkProviderProxy client = getProviderProxyFactory().create(provider);
        List<Network> externalNetworks = client.getAll();

        Map<Network, Set<Guid>> externalNetworkToDcId = new HashMap<>();
        for (Network network : externalNetworks) {
            List<Guid> dcIds =
                    getDbFacade().getStoragePoolDao().getDcIdByExternalNetworkId(network.getProvidedBy().getExternalId());
            externalNetworkToDcId.put(network, new HashSet<>(dcIds));
        }

        getQueryReturnValue().setReturnValue(externalNetworkToDcId);
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }
}
