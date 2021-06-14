package org.ovirt.engine.core.bll.storage.backup;

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
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for clearing completed backups and image transfers from the database by running a fixed scheduled job
 * every {@link ConfigValues#DbEntitiesCleanupRateInMinutes} minutes.
 */
@Singleton
public class DbEntityCleanupManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(DbEntityCleanupManager.class);
    private int succeededBackupTime;
    private int failedBackupTime;
    private int succeededImageTransferTime;
    private int failedImageTransferTime;

    @Inject
    private VmBackupDao vmBackupDao;
    @Inject
    private ImageTransferDao imageTransferDao;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private DbEntityCleanupManager() {
    }

    /**
     * Initializes the Backup Cleanup scheduler.
     */
    @PostConstruct
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        succeededBackupTime = Config.<Integer> getValue(ConfigValues.SucceededBackupCleanupTimeInMinutes);
        failedBackupTime = Config.<Integer> getValue(ConfigValues.FailedBackupCleanupTimeInMinutes);
        succeededImageTransferTime = Config.<Integer> getValue(ConfigValues.SucceededImageTransferCleanupTimeInMinutes);
        failedImageTransferTime = Config.<Integer> getValue(ConfigValues.FailedImageTransferCleanupTimeInMinutes);

        long cleanupFrequency = Config.<Integer> getValue(ConfigValues.DbEntitiesCleanupRateInMinutes);
        executor.scheduleWithFixedDelay(this::cleanCompletedDbEntities,
                cleanupFrequency,
                cleanupFrequency,
                TimeUnit.MINUTES);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * This method is scheduled to run every certain period of time and
     * remove database entities that are not needed anymore.
     *
     * Completed backups:
     * <ul>
     * <li>Successful backups will be deleted after {@link ConfigValues#SucceededBackupCleanupTimeInMinutes}.</li>
     * <li>Failed backups will be deleted after {@link ConfigValues#FailedBackupCleanupTimeInMinutes}.</li>
     * </ul>
     * Completed image transfers:
     * <ul>
     * <li>Successful image transfers will be deleted after {@link ConfigValues#SucceededImageTransferCleanupTimeInMinutes}.</li>
     * <li>Failed image transfers will be deleted after {@link ConfigValues#FailedImageTransferCleanupTimeInMinutes}.</li>
     * </ul>
     */
    private void cleanCompletedDbEntities() {
        long currentTimeMillis = System.currentTimeMillis();
        Date succeededBackupsDeleteTime =
                new Date(currentTimeMillis - TimeUnit.MILLISECONDS.convert(succeededBackupTime, TimeUnit.MINUTES));
        Date failedBackupsDeleteTime =
                new Date(currentTimeMillis - TimeUnit.MILLISECONDS.convert(failedBackupTime, TimeUnit.MINUTES));
        Date succeededImageTransfersDeleteTime =
                new Date(currentTimeMillis - TimeUnit.MILLISECONDS.convert(succeededImageTransferTime, TimeUnit.MINUTES));
        Date failedImageTransfersDeleteTime =
                new Date(currentTimeMillis - TimeUnit.MILLISECONDS.convert(failedImageTransferTime, TimeUnit.MINUTES));

        try {
            vmBackupDao.deleteCompletedBackups(succeededBackupsDeleteTime, failedBackupsDeleteTime);
        } catch (Throwable t) {
            log.error("Failed to delete completed backups: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
        try {
            imageTransferDao.deleteCompletedImageTransfers(succeededImageTransfersDeleteTime, failedImageTransfersDeleteTime);
        } catch (Throwable t) {
            log.error("Failed to delete completed image transfers: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }
}
