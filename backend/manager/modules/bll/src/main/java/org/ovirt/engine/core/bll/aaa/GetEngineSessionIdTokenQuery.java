package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetEngineSessionIdTokenQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetEngineSessionIdTokenQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(FiltersHelper.getTokenInstance(getParameters().getSessionId()));
        getQueryReturnValue().setSucceeded(true);
    }
}
