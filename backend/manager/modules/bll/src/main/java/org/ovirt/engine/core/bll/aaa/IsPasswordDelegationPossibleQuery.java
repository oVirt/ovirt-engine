package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class IsPasswordDelegationPossibleQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private SessionDataContainer sessionDataContainer;

    public IsPasswordDelegationPossibleQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(sessionDataContainer.getPassword(getParameters().getSessionId()) != null);
        getQueryReturnValue().setSucceeded(true);
    }
}
