/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetExternalSubnetsOnProviderByExternalNetworkQuery
        <P extends GetExternalSubnetsOnProviderByExternalNetworkQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao providerDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetExternalSubnetsOnProviderByExternalNetworkQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = providerDao.get(getParameters().getProviderId());
        if (provider == null) {
            return;
        }

        NetworkProviderProxy client = providerProxyFactory.create(provider);
        ProviderNetwork providedBy = new ProviderNetwork();
        providedBy.setProviderId(getParameters().getProviderId());
        providedBy.setExternalId(getParameters().getNetworkId());
        getQueryReturnValue().setReturnValue(client.getAllSubnets(providedBy));
    }
}
