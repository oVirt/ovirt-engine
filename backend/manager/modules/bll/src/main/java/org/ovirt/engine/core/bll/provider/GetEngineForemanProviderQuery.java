package org.ovirt.engine.core.bll.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.host.provider.foreman.EngineForemanProviderFinder;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * Retrieves the Foreman provider for the ovirt-engine server or <code>null</code> if wasn't found
 */
public class GetEngineForemanProviderQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private EngineForemanProviderFinder engineProviderFinder;

    public GetEngineForemanProviderQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(engineProviderFinder.findEngineProvider());
    }
}
