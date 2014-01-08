package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

/** A query to return all the Audit Logs according to a given VM Template ID */
public class GetAllAuditLogsByVMTemplateIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMTemplateIdQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAuditLogDao().getAllByVMTemplateId(
                        getParameters().getId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
