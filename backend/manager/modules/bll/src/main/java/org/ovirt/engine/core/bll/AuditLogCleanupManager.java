package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class AuditLogCleanupManager {
    private static LogCompat log = LogFactoryCompat.getLog(AuditLogCleanupManager.class);

    private static final AuditLogCleanupManager _instance = new AuditLogCleanupManager();

    public static AuditLogCleanupManager getInstance() {
        return _instance;
    }

    private AuditLogCleanupManager() {
        Calendar calendar = new GregorianCalendar();
        Date mAuditLogCleanupTime = Config.<DateTime> GetValue(ConfigValues.AuditLogCleanupTime);
        calendar.setTimeInMillis(mAuditLogCleanupTime.getTime());

        String cronExpression = String.format("%d %d %d * * ?", calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY));

        log.info("Setting audit clean up manager to run at: " + cronExpression);
        SchedulerUtilQuartzImpl.getInstance().scheduleACronJob(this, "OnTimer", new Class[] {}, new Object[] {},
                cronExpression);
    }

    @OnTimerMethodAnnotation("OnTimer")
    public void OnTimer() {
        try {
            log.info("AuditLogCleanupManager::deleteAgedOutAuditLogs - entered");
            DateTime latestTimeToKeep = DateTime.getNow().AddDays(
                    Config.<Integer> GetValue(ConfigValues.AuditLogAgingThreashold) * -1);
            DbFacade.getInstance().getAuditLogDAO().removeAllBeforeDate(latestTimeToKeep);
        } catch (RuntimeException e) {
            log.error("AuditLogCleanupManager::deleteAgedOutAuditLogs() - failed with exception", e);
        }
    }

}
