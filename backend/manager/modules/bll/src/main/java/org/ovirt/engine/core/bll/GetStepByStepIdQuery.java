package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Returns a Step by its job-ID
 */
public class GetStepByStepIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetStepByStepIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(JobRepositoryFactory.getJobRepository()
                .getStep(getParameters().getId()));
    }
}
