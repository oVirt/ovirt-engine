package org.ovirt.engine.core.bll.provider.network;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllExternalNetworksOnProviderQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllExternalNetworksOnProviderQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = getDbFacade().getProviderDao().get(getParameters().getId());
        if (provider != null) {
            NetworkProviderProxy client = getProviderProxyFactory().create(provider);
            getQueryReturnValue().setReturnValue(client.getAll());
        }
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }
}
