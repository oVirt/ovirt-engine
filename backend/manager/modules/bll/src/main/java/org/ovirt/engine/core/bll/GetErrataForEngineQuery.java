package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetErrataForEngineQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

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
        } else {
            setReturnValue(proxy.getErrataForHost(engineProviderFinder.getEngineHostName()));
        }
    }
}
