package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.SystemProviderFinder;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetErratumByIdForSystemQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private SystemProviderFinder systemProviderFinder;

    public GetErratumByIdForSystemQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = systemProviderFinder.findSystemProvider();
        if (proxy != null) {
            setReturnValue(proxy.getErratumForHost(systemProviderFinder.getSystemHostName(), getParameters().getName()));
        }
    }
}
