package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;

public class GetAuditLogByIdQuery <P extends GetAuditLogByIdParameters> extends QueriesCommandBase<P>{

    public GetAuditLogByIdQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAuditLogDao()
                        .get(getParameters().getId()));
    }
}
