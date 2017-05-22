package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.queries.GetJobsByOffsetQueryParameters;
import org.ovirt.engine.core.dao.JobDao;

/**
 * Returns a list of Jobs by a given offset
 */
public class GetJobsByOffsetQuery<P extends GetJobsByOffsetQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private JobDao jobDao;

    public GetJobsByOffsetQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Defines the page size for retrieving the Jobs
        final int pageSize = Config.<Integer> getValue(ConfigValues.JobPageSize);

        int offset = Math.max(getParameters().getOffset(), 0);
        List<Job> jobs = jobDao.getJobsByOffsetAndPageSize(offset * pageSize, pageSize);
        getQueryReturnValue().setReturnValue(jobs);
    }

}
