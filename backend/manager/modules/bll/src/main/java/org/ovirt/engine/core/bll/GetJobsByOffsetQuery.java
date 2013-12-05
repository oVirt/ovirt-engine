package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.queries.GetJobsByOffsetQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Returns a list of Jobs by a given offset
 */
public class GetJobsByOffsetQuery<P extends GetJobsByOffsetQueryParameters> extends QueriesCommandBase<P> {

    public GetJobsByOffsetQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Defines the page size for retrieving the Jobs
        final int pageSize = Config.<Integer> getValue(ConfigValues.JobPageSize);

        int offset = Math.max(getParameters().getOffset(), 0);
        List<Job> jobs = DbFacade.getInstance().getJobDao().getJobsByOffsetAndPageSize(offset * pageSize, pageSize);
        getQueryReturnValue().setReturnValue(jobs);
    }

}
