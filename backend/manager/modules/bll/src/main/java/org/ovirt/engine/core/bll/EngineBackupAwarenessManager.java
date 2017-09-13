package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.EngineBackupLogDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for checking that a warm backup is available
 * raise alerts for no backup or too old backup.
 */
@Singleton
public class EngineBackupAwarenessManager implements BackendService {

    private enum BackupScope {
        DB("db"),
        FILES("files");

        String name;

        BackupScope(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EngineBackupAwarenessManager.class);
    private Lock lock = new ReentrantLock();
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private EngineBackupLogDao engineBackupLogDao;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    /**
     * Initializes the backup h Check Manager
     */
    @PostConstruct
    private void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        long backupCheckPeriodInHours = Config.<Long>getValue(ConfigValues.BackupCheckPeriodInHours);
        // disable feature if value is negative
        if (backupCheckPeriodInHours > 0) {
            executor.scheduleWithFixedDelay(this::backupCheck,
                    backupCheckPeriodInHours,
                    backupCheckPeriodInHours,
                    TimeUnit.HOURS);
            log.info("Finished initializing {}", getClass().getSimpleName());
        }
    }

    private void backupCheck() {
        try {
            // skip backup check if previous operation is not completed yet
            if (lock.tryLock()) {
                try {
                    log.info("Backup check started.");
                    doBackupCheck();
                    log.info("Backup check completed.");
                } finally {
                    lock.unlock();
                }
            }
        } catch (Throwable t) {
            log.error("Exception in backupCheck: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private void doBackupCheck() {
        AuditLogable alert = new AuditLogableImpl();

        //try to get last backup record
        EngineBackupLog lastDbBackup = getLastBackupByScope(BackupScope.DB);
        EngineBackupLog lastFilesBackup = getLastBackupByScope(BackupScope.FILES);
        if (lastDbBackup == null || lastFilesBackup == null) {
            auditLogDirector.log(alert, AuditLogType.ENGINE_NO_FULL_BACKUP);
        } else {
            //check time elapsed from last full (db and files) backup
            Integer backupAlertPeriodInDays = Config.<Integer>getValue(ConfigValues.BackupAlertPeriodInDays);
            Date lastDbBackupDate = lastDbBackup.getDoneAt();
            Date lastFilesBackupDate = lastFilesBackup.getDoneAt();
            Date lastFullBackupDate = lastDbBackupDate.compareTo(lastFilesBackupDate) < 0
                    ? lastDbBackupDate
                    : lastFilesBackupDate;
            long diffInDays = (Calendar.getInstance().getTimeInMillis() - lastFullBackupDate.getTime())
                              / TimeUnit.DAYS.toMillis(1);
            if (diffInDays > backupAlertPeriodInDays) {
                alert.addCustomValue("Date", lastFullBackupDate.toString());
                auditLogDirector.log(alert, AuditLogType.ENGINE_NO_WARM_BACKUP);
            }
        }

    }

    private EngineBackupLog getLastBackupByScope(BackupScope scope) {
        return engineBackupLogDao.getLastSuccessfulEngineBackup(scope.getName());
    }
}
