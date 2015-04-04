package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
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
public class EngineBackupAwarenessManager {
    private static final Logger log = LoggerFactory.getLogger(EngineBackupAwarenessManager.class);
    private static final String ENGINE_DB_ID = "engine";
    private volatile boolean active;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private EngineBackupLogDao engineBackupLogDao;

    /**
     * Initializes the backup h Check Manager
     */
    public void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Integer backupCheckPeriodInHours = Config.<Integer>getValue(ConfigValues.BackupCheckPeriodInHours);
        // disable feature if value is negative
        if (backupCheckPeriodInHours > 0) {
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                    "backupCheck",
                    new Class[] { },
                    new Object[] { },
                    backupCheckPeriodInHours,
                    backupCheckPeriodInHours,
                    TimeUnit.HOURS);
            log.info("Finished initializing {}", getClass().getSimpleName());
        }
    }

    @OnTimerMethodAnnotation("backupCheck")
    public void backupCheck() {
        // skip backup check if previous operation is not completed yet
        if (!active) {
            try {
                synchronized (this) {
                    log.info("Backup check started.");
                    active = true;
                    doBackupCheck();
                    log.info("Backup check completed.");
                }
            } finally {
                active = false;
            }
        }
    }

    private void doBackupCheck() {
        AuditLogableBase alert = new AuditLogableBase();

        //try to get last backup record
        EngineBackupLog lastBackup = engineBackupLogDao.getLastSuccessfulEngineBackup(ENGINE_DB_ID);
        if (lastBackup == null) {
            auditLogDirector.log(alert, AuditLogType.ENGINE_NO_BACKUP);
        } else {
            //check time elapsed from last backup
            Integer backupAlertPeriodInDays = Config.<Integer>getValue(ConfigValues.BackupAlertPeriodInDays);
            Date lastBackupDate = lastBackup.getDoneAt();
            long diffInDays = (Calendar.getInstance().getTimeInMillis() - lastBackupDate.getTime()) / TimeUnit.DAYS.toMillis(1);
            if (diffInDays > backupAlertPeriodInDays) {
                alert.addCustomValue("Date", lastBackupDate.toString());
                auditLogDirector.log(alert, AuditLogType.ENGINE_NO_WARM_BACKUP);
            }
        }

    }
}
