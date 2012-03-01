package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllAuditLogsByVMNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/** A query to return all the Audit Logs according to a given VM name */
public class GetAllAuditLogsByVMNameQuery<P extends GetAllAuditLogsByVMNameParameters> extends QueriesCommandBase<P> {

    public GetAllAuditLogsByVMNameQuery(P parameters) {
        super(parameters);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getAuditLogDAO()
                        .getAllByVMName(getParameters().getVmName(), getUserID(), getParameters().isFiltered()));
    }
}
