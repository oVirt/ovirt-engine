package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

/** A query to return all the Audit Logs according to a given VM name */
public class GetAllAuditLogsByVMNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMNameQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAuditLogDao()
                        .getAllByVMName(getParameters().getName(), getUserID(), getParameters().isFiltered()));
    }
}
