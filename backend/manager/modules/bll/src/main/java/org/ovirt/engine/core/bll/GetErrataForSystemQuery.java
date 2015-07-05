package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.SystemProviderFinder;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetErrataForSystemQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private SystemProviderFinder systemProviderFinder;

    public GetErrataForSystemQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        HostProviderProxy proxy = systemProviderFinder.findSystemProvider();
        if (proxy == null) {
            getQueryReturnValue().setSucceeded(false);
            getQueryReturnValue().setExceptionString(EngineMessage.NO_HOST_PROVIDER_FOR_SYSTEM.name());
        } else {
            setReturnValue(proxy.getErrataForHost(systemProviderFinder.getSystemHostName()));
        }
    }
}
