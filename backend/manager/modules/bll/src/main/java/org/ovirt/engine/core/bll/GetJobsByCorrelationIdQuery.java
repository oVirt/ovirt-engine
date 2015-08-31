package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.job.Job;
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
        List<Job> jobs = DbFacade.getInstance().getJobDao().getJobsByCorrelationId(getParameters().getCorrelationId());

        for (Job job : jobs) {
            JobRepositoryFactory.getJobRepository().loadJobSteps(job);
        }

        getQueryReturnValue().setReturnValue(jobs);
    }
}
