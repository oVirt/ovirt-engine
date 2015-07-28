package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class IsPasswordDelegationPossibleQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public IsPasswordDelegationPossibleQuery(P parameters) {
        super(parameters);
    }

    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(SessionDataContainer.getInstance().getPassword(getParameters().getSessionId()) != null);
        getQueryReturnValue().setSucceeded(true);
    }
}
