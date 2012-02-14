package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetJobsByCorrelationIdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Returns a list of Jobs associated with the same correlation-ID
 */
public class GetJobsByCorrelationIdQuery<P extends GetJobsByCorrelationIdQueryParameters> extends QueriesCommandBase<P> {

    public GetJobsByCorrelationIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getJobDao()
                .getJobsByCorrelationId(getParameters().getCorrelationId()));
    }
}
