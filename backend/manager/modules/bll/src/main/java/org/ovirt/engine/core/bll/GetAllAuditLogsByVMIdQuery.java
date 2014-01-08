package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/** A query to return all the Audit Logs according to a given VM ID */
public class GetAllAuditLogsByVMIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMIdQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAuditLogDao()
                        .getAllByVMId(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
