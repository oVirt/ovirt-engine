package org.ovirt.engine.core.bll.job;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for clearing completed jobs from the database by running a fixed scheduled job
 * each {@code ConfigValues.JobCleanupRateInMinutes} minutes.
 */
public class JobRepositoryCleanupManager {

    private static final Logger log = LoggerFactory.getLogger(JobRepositoryCleanupManager.class);
    private static JobRepositoryCleanupManager instance = new JobRepositoryCleanupManager();
    private int succeededJobTime;
    private int failedJobTime;

    private JobRepositoryCleanupManager() {
    }

    /**
     * Initializes the Job Cleanup scheduler
     */
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        succeededJobTime = Config.<Integer> getValue(ConfigValues.SucceededJobCleanupTimeInMinutes).intValue();
        failedJobTime = Config.<Integer> getValue(ConfigValues.FailedJobCleanupTimeInMinutes).intValue();

        Integer cleanupFrequency = Config.<Integer> getValue(ConfigValues.JobCleanupRateInMinutes);
        Injector.get(SchedulerUtilQuartzImpl.class).scheduleAFixedDelayJob(this,
                "completed_jobs_cleanup",
                new Class[] {},
                new Object[] {},
                cleanupFrequency,
                cleanupFrequency,
                TimeUnit.MINUTES);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Removes completed jobs:
     * <ul>
     * <li>The successful jobs will be deleted after {@code ConfigValues#SucceededJobCleanupTimeInMinutes}.</li>
     * <li>The failed jobs will be deleted after {@code ConfigValues#FailedJobCleanupTimeInMinutes}.</li>
     * </ul>
     */
    @OnTimerMethodAnnotation("completed_jobs_cleanup")
    public void cleanCompletedJob() {

        Date succeededJobsDeleteTime =
                new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(succeededJobTime, TimeUnit.MINUTES));
        Date failedJobsDeleteTime =
                new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(failedJobTime, TimeUnit.MINUTES));

        try {
            DbFacade.getInstance().getJobDao().deleteCompletedJobs(succeededJobsDeleteTime, failedJobsDeleteTime);
        } catch (RuntimeException e) {
            log.error("Failed to delete completed jobs: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

    public static JobRepositoryCleanupManager getInstance() {
        return instance;
    }
}
