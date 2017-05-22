package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.queries.GetJobsByCorrelationIdQueryParameters;
import org.ovirt.engine.core.dao.JobDao;

/**
 * Returns a list of Jobs associated with the same correlation-ID
 */
public class GetJobsByCorrelationIdQuery<P extends GetJobsByCorrelationIdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private JobRepository jobRepository;

    @Inject
    protected JobDao jobDao;

    public GetJobsByCorrelationIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Job> jobs = jobDao.getJobsByCorrelationId(getParameters().getCorrelationId());

        for (Job job : jobs) {
            jobRepository.loadJobSteps(job);
        }

        getQueryReturnValue().setReturnValue(jobs);
    }
}
