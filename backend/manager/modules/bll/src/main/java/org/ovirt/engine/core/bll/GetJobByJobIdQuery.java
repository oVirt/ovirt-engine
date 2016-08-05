package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Returns a Job by its job-ID
 */
public class GetJobByJobIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private JobRepository jobRepository;

    public GetJobByJobIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(jobRepository.getJobWithSteps(getParameters().getId()));
    }
}
