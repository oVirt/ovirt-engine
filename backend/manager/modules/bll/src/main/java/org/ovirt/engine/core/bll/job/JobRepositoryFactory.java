package org.ovirt.engine.core.bll.job;

import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Factory responsible for instantiating the {@link JobRepository}
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
