package org.ovirt.engine.core.bll.job;

import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.JobRepository;
import org.ovirt.engine.core.dal.job.JobRepositoryImpl;

/**
 * Factory responsible for instantiating the {@JobRepository}
 */
public class JobRepositoryFactory {

    private static JobRepository jobRepository;

    static {
        jobRepository = new JobRepositoryImpl(DbFacade.getInstance().getJobDao(),
                                              DbFacade.getInstance().getJobSubjectEntityDao(),
                                              DbFacade.getInstance().getStepDao());
    }

    public static JobRepository getJobRepository() {
        return jobRepository;
    }

}
