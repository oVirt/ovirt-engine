package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
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

    /**
     * A comparator class for returning sorted jobs list.
     * <ul>
     * <li>Primary search: Job's status</li>
     * <li>Secondary search: Job's start time</li>
     * </ul>
     */
    private static Comparator<Job> jobComparator = new Comparator<Job>() {

        @Override
        public int compare(Job o1, Job o2) {
            int result = o1.getStatus().compareTo(o2.getStatus());
            if (result == 0) {
                result = o1.getStartTime().compareTo(o2.getStartTime());
            }
            return result;
        }

    };

    public GetJobsByOffsetQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        int offset = Math.max(getParameters().getOffset(), 0);
        List<Job> jobs = DbFacade.getInstance().getJobDao().getJobsByOffsetAndPageSize(offset * pageSize, pageSize);
        Collections.sort(jobs, jobComparator);
        getQueryReturnValue().setReturnValue(jobs);
    }

}
