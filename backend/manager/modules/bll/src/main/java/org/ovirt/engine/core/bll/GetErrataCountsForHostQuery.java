package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataCountsForHostQuery<P extends GetErrataCountsParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetErrataCountsForHostQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = vdsStaticDao.get(getParameters().getId());
        if (host == null) {
            failWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return;
        }

        Provider<?> provider = getHostProvider(host);
        if (provider == null) {
            failWith(EngineMessage.NO_FOREMAN_PROVIDER_FOR_HOST);
            return;
        }

        HostProviderProxy proxy = getHostProviderProxy(provider);

        ContentHostIdentifier contentHostIdentifier = ContentHostIdentifier.builder()
                .withId(host.getUniqueID())
                .withName(host.getHostName())
                .build();

        ErrataData errataForHost = proxy.getErrataForHost(contentHostIdentifier, getParameters().getErrataFilter());
        setReturnValue(errataForHost.getErrataCounts());
    }

    private void failWith(EngineMessage failure) {
        getQueryReturnValue().setExceptionString(failure.name());
        getQueryReturnValue().setSucceeded(false);
    }

    HostProviderProxy getHostProviderProxy(Provider<?> provider) {
        return (HostProviderProxy) providerProxyFactory.create(provider);
    }

    private Provider<?> getHostProvider(VdsStatic host) {
        return host.getHostProviderId() == null ? null : providerDao.get(host.getHostProviderId());
    }
}
