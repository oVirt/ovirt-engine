package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Returns a Job by its job-ID
 */
public class GetJobByJobIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private JobRepository jobRepository;

    public GetJobByJobIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Job jobWithSteps = jobRepository.getJobWithSteps(getParameters().getId());
        if (getParameters().isFiltered()) {
            Guid ownerId = getUserID();
            if (jobWithSteps.getOwnerId().equals(ownerId)) {
                getQueryReturnValue().setReturnValue(jobWithSteps);
            }
        } else {
            getQueryReturnValue().setReturnValue(jobWithSteps);
        }
    }
}
