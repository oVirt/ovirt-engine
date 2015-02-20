package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;


public class GetAllVmsForAnotherUserQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllVmsForAnotherUserQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsForAnotherUserQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        /*
           this query is meant to be invoked by ovirt-vmconsole helper(s).
           Since it fetches information from the backend without restrictions,
           we need to block it for non-internal calls.
         */
        if (isInternalExecution()) {
            getQueryReturnValue().setReturnValue(
                    getDbFacade().getVmDao().getAllForUser(getParameters().getId()));
        }
    }
}
