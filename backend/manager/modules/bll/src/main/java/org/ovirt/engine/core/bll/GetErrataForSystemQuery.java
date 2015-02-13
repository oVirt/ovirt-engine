package org.ovirt.engine.core.bll;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.host.provider.foreman.SystemProviderFinder;
import org.ovirt.engine.core.common.businessentities.Erratum;
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
            setReturnValue(Collections.<Erratum> emptyList());
        } else {
            setReturnValue(proxy.getErrataForHost(systemProviderFinder.getSystemHostName()));
        }
    }
}
