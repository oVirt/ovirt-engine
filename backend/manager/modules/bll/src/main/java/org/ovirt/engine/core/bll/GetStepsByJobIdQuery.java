package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetStepsByJobIdQueryParameters;

public class GetStepsByJobIdQuery <P extends GetStepsByJobIdQueryParameters> extends QueriesCommandBase<P> {

    public GetStepsByJobIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStepDao().getStepsByJobId(getParameters().getJobId()));
    }
}
