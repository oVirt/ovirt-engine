package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;

public class GetErrataForEngineQuery<P extends GetErrataCountsParameters> extends QueriesCommandBase<P> {

    @Inject
    private EngineForemanProviderFinder engineProviderFinder;

    public GetErrataForEngineQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = engineProviderFinder.findEngineProvider();
        if (proxy == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(EngineMessage.NO_FOREMAN_PROVIDER_FOR_ENGINE.name());
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
        } else {
            ContentHostIdentifier contentHostIdentifier = ContentHostIdentifier.builder()
                    .withName(engineProviderFinder.getEngineHostName())
                    .build();

            ErrataData errataForEngine =
                    proxy.getErrataForHost(contentHostIdentifier, getParameters().getErrataFilter());
            setReturnValue(errataForEngine);
        }
    }
}
