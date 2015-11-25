package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.EngineBackupLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.EngineBackupLogDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for checking that a warm backup is available
 * raise alerts for no backup or too old backup.
 */
@Singleton
public class EngineBackupAwarenessManager implements BackendService {

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

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

    /**
     * Initializes the backup h Check Manager
     */
    @PostConstruct
    private void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Integer backupCheckPeriodInHours = Config.<Integer>getValue(ConfigValues.BackupCheckPeriodInHours);
        // disable feature if value is negative
        if (backupCheckPeriodInHours > 0) {
            schedulerUtil.scheduleAFixedDelayJob(this,
                    "backupCheck",
                    new Class[] {},
                    new Object[] {},
                    backupCheckPeriodInHours,
                    backupCheckPeriodInHours,
                    TimeUnit.HOURS);
            log.info("Finished initializing {}", getClass().getSimpleName());
        }
    }

    @OnTimerMethodAnnotation("backupCheck")
    public void backupCheck() {
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
    }

    private void doBackupCheck() {
        AuditLogableBase alert = new AuditLogableBase();

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
