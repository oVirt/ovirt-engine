package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllJobsQuery <P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllJobsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getJobDao().getAll());
    }
}
