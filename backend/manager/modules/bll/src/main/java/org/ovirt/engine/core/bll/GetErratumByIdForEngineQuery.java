package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetErratumByIdForEngineQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private EngineForemanProviderFinder engineProviderFinder;

    public GetErratumByIdForEngineQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = engineProviderFinder.findEngineProvider();
        if (proxy != null) {
            ContentHostIdentifier contentHostIdentifier = ContentHostIdentifier.builder()
                    .withName(engineProviderFinder.getEngineHostName())
                    .build();
            setReturnValue(proxy.getErratumForHost(contentHostIdentifier, getParameters().getName()));
        }
    }
}
