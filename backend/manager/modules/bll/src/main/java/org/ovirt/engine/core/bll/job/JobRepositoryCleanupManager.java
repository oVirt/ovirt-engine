package org.ovirt.engine.core.bll.job;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for clearing completed jobs from the database by running a fixed scheduled job
 * each {@code ConfigValues.JobCleanupRateInMinutes} minutes.
 */
@Singleton
public class JobRepositoryCleanupManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(JobRepositoryCleanupManager.class);
    private int succeededJobTime;
    private int failedJobTime;

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    @Inject
    private JobDao jobDao;

    private JobRepositoryCleanupManager() {
    }

    /**
     * Initializes the Job Cleanup scheduler
     */
    @PostConstruct
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        succeededJobTime = Config.<Integer> getValue(ConfigValues.SucceededJobCleanupTimeInMinutes);
        failedJobTime = Config.<Integer> getValue(ConfigValues.FailedJobCleanupTimeInMinutes);

        Integer cleanupFrequency = Config.<Integer> getValue(ConfigValues.JobCleanupRateInMinutes);
        schedulerUtil.scheduleAFixedDelayJob(this,
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
            jobDao.deleteCompletedJobs(succeededJobsDeleteTime, failedJobsDeleteTime);
        } catch (RuntimeException e) {
            log.error("Failed to delete completed jobs: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }
}
