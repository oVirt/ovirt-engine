package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class UnregisterQueryQuery<P extends UnregisterQueryParameters> extends QueriesCommandBase<P> {
    public UnregisterQueryQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        BackendCallBacksDirector.getInstance().UnregisterQuery(getParameters().getQueryID());
    }

    private void Run(Object stateInfo) {
        VdcQueryParametersBase tempVar = getParameters();
        BackendCallBacksDirector.getInstance().UnregisterQuery(
                ((UnregisterQueryParameters) ((tempVar instanceof UnregisterQueryParameters) ? tempVar : null))
                        .getQueryID());
    }
}
