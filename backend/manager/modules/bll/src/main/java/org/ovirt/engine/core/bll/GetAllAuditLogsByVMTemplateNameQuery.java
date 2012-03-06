package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllAuditLogsByVMTemplateNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/** A query to return all the Audit Logs according to a given VM Template name */
public class GetAllAuditLogsByVMTemplateNameQuery<P extends GetAllAuditLogsByVMTemplateNameParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMTemplateNameQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance()
                        .getAuditLogDAO()
                        .getAllByVMTemplateName(getParameters().getVmTemplateName(),
                                getUserID(),
                                getParameters().isFiltered()));
    }
}
