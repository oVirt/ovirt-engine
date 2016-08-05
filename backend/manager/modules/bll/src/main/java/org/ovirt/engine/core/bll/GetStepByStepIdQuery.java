package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Returns a Step by its job-ID
 */
public class GetStepByStepIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private JobRepository jobRepository;

    public GetStepByStepIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(jobRepository.getStep(getParameters().getId()));
    }
}
