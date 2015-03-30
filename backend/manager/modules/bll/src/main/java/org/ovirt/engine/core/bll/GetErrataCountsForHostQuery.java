package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetErrataCountsForHostQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetErrataCountsForHostQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic host = getDbFacade().getVdsStaticDao().get(getParameters().getId());
        if (host == null) {
            failWith(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            return;
        }

        Provider<?> provider = getHostProvider(host);
        if (provider == null) {
            failWith(VdcBllMessages.NO_HOST_PROVIDER_FOR_SYSTEM);
            return;
        }

        HostProviderProxy proxy = getHostProviderProxy(provider);
        List<Erratum> errata = proxy.getErrataForHost(host.getHostName());
        ErrataCounts stats = new ErrataCounts();
        for (Erratum erratum : errata) {
            stats.addToCounts(erratum);
        }

        setReturnValue(stats);
    }

    private void failWith(VdcBllMessages failure) {
        getQueryReturnValue().setExceptionString(failure.name());
        getQueryReturnValue().setSucceeded(false);
    }

    HostProviderProxy getHostProviderProxy(Provider<?> provider) {
        return (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
    }

    private Provider<?> getHostProvider(VdsStatic host) {
        return host.getHostProviderId() == null ? null : getDbFacade().getProviderDao().get(host.getHostProviderId());
    }
}
