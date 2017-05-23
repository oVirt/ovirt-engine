package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataForHostQuery<P extends GetErrataCountsParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsStaticDao hostStaticDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetErrataForHostQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = hostStaticDao.get(getParameters().getId());
        if (host == null || host.getHostProviderId() == null) {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
            return;
        }

        Provider<?> provider = providerDao.get(host.getHostProviderId());
        if (provider != null) {
            HostProviderProxy proxy = providerProxyFactory.create(provider);
            ErrataData errataForHost = proxy.getErrataForHost(host.getHostName(),
                    getParameters().getErrataFilter());
            getQueryReturnValue().setReturnValue(errataForHost);
        } else {
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
        }
    }
}
