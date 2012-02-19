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

    /**
     * Defines the page size for retrieving the Jobs
     */
    private static int pageSize = Config.<Integer> GetValue(ConfigValues.JobPageSize);

    public GetJobsByOffsetQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        int offset = Math.max(getParameters().getOffset(), 0);
        List<Job> jobs = DbFacade.getInstance().getJobDao().getJobsByOffsetAndPageSize(offset * pageSize, pageSize);
        getQueryReturnValue().setReturnValue(jobs);
    }

}
