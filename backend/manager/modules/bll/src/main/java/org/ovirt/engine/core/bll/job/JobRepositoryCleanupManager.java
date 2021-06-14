package org.ovirt.engine.core.bll.job;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for clearing completed jobs from the database by running a fixed scheduled job
 * each {@link ConfigValues#JobCleanupRateInMinutes} minutes.
 */
@Singleton
public class JobRepositoryCleanupManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(JobRepositoryCleanupManager.class);
    private int succeededJobTime;
    private int failedJobTime;

    @Inject
    private JobDao jobDao;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private JobRepositoryCleanupManager() {
    }

    /**
     * Initializes the Job Cleanup scheduler.
     */
    @PostConstruct
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        succeededJobTime = Config.<Integer> getValue(ConfigValues.SucceededJobCleanupTimeInMinutes);
        failedJobTime = Config.<Integer> getValue(ConfigValues.FailedJobCleanupTimeInMinutes);

        long cleanupFrequency = Config.<Long> getValue(ConfigValues.JobCleanupRateInMinutes);
        executor.scheduleWithFixedDelay(this::cleanCompletedJob,
                cleanupFrequency,
                cleanupFrequency,
                TimeUnit.MINUTES);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Removes completed jobs:
     * <ul>
     * <li>The successful jobs will be deleted after {@link ConfigValues#SucceededJobCleanupTimeInMinutes}.</li>
     * <li>The failed jobs will be deleted after {@link ConfigValues#FailedJobCleanupTimeInMinutes}.</li>
     * </ul>
     */
    private void cleanCompletedJob() {

        Date succeededJobsDeleteTime =
                new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(succeededJobTime, TimeUnit.MINUTES));
        Date failedJobsDeleteTime =
                new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(failedJobTime, TimeUnit.MINUTES));

        try {
            jobDao.deleteCompletedJobs(succeededJobsDeleteTime, failedJobsDeleteTime);
        } catch (Throwable t) {
            log.error("Failed to delete completed jobs: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }
}
