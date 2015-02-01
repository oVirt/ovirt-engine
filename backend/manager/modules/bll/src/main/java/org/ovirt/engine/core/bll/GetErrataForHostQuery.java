package org.ovirt.engine.core.bll;

import java.util.Collections;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetErrataForHostQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetErrataForHostQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = getDbFacade().getVdsStaticDao().get(getParameters().getId());
        if (host == null || host.getHostProviderId() == null) {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
            return;
        }

        Provider<?> provider = getDbFacade().getProviderDao().get(host.getHostProviderId());
        if (provider != null) {
            HostProviderProxy proxy = (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
            getQueryReturnValue().setReturnValue(proxy.getErrataForHost(host));
        } else {
            getQueryReturnValue().setReturnValue(Collections.<Erratum> emptyList());
        }
    }
}
