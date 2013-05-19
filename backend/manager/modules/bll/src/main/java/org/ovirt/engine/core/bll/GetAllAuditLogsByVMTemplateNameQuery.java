package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

/** A query to return all the Audit Logs according to a given VM Template name */
public class GetAllAuditLogsByVMTemplateNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMTemplateNameQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getAuditLogDao().getAllByVMTemplateName(
                        getParameters().getName(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
