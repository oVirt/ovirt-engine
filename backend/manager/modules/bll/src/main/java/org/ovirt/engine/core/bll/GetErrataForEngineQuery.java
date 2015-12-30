package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;

public class GetErrataForEngineQuery<P extends GetErrataCountsParameters> extends QueriesCommandBase<P> {

    @Inject
    private EngineForemanProviderFinder engineProviderFinder;

    public GetErrataForEngineQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = engineProviderFinder.findEngineProvider();
        if (proxy == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(EngineMessage.NO_FOREMAN_PROVIDER_FOR_ENGINE.name());
            getQueryReturnValue().setReturnValue(ErrataData.emptyData());
        } else {
            ErrataData errataForEngine = proxy.getErrataForHost(engineProviderFinder.getEngineHostName(),
                    getParameters().getErrataFilter());
            setReturnValue(errataForEngine);
        }
    }
}
