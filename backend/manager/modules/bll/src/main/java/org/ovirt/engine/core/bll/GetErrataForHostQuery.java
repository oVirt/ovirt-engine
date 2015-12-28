package org.ovirt.engine.core.bll;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetErrataForHostQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsStaticDao hostStaticDao;

    @Inject
    private ProviderDao providerDao;

    public GetErrataForHostQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = hostStaticDao.get(getParameters().getId());
        if (host == null || host.getHostProviderId() == null) {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
            return;
        }

        Provider<?> provider = providerDao.get(host.getHostProviderId());
        if (provider != null) {
            HostProviderProxy proxy = ProviderProxyFactory.getInstance().create(provider);
            getQueryReturnValue().setReturnValue(proxy.getErrataForHost(host.getHostName()));
        } else {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
        }
    }
}
