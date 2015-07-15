package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogCleanupManager {
    private static final Logger log = LoggerFactory.getLogger(AuditLogCleanupManager.class);

    private static final AuditLogCleanupManager instance = new AuditLogCleanupManager();

    public static AuditLogCleanupManager getInstance() {
        return instance;
    }

    private AuditLogCleanupManager() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Calendar calendar = new GregorianCalendar();
        Date auditLogCleanupTime = Config.<DateTime> getValue(ConfigValues.AuditLogCleanupTime);
        calendar.setTimeInMillis(auditLogCleanupTime.getTime());

        String cronExpression = String.format("%d %d %d * * ?", calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY));

        log.info("Setting audit cleanup manager to run at '{}'", cronExpression);
        Injector.get(SchedulerUtilQuartzImpl.class).scheduleACronJob(this, "onTimer", new Class[] {}, new Object[] {},
                cronExpression);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        try {
            log.info("Start deleteAgedOutAuditLogs");
            DateTime latestTimeToKeep = DateTime.getNow().addDays(
                    Config.<Integer>getValue(ConfigValues.AuditLogAgingThreshold)
                            * -1);
            DbFacade.getInstance().getAuditLogDao().removeAllBeforeDate(latestTimeToKeep);
            log.info("Finished deleteAgedOutAuditLogs");
        } catch (RuntimeException e) {
            log.error("deleteAgedOutAuditLog failed with exception", e);
        }
    }

}
