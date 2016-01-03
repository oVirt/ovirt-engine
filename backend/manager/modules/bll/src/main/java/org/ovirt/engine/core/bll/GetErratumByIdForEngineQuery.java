package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetErratumByIdForEngineQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private EngineForemanProviderFinder engineProviderFinder;

    public GetErratumByIdForEngineQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = engineProviderFinder.findEngineProvider();
        if (proxy != null) {
            setReturnValue(proxy.getErratumForHost(engineProviderFinder.getEngineHostName(), getParameters().getName()));
        }
    }
}
