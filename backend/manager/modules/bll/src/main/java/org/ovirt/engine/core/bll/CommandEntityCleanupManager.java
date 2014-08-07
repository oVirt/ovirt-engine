package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CommandEntityCleanupManager {
    private static Log log = LogFactory.getLog(AuditLogCleanupManager.class);

    private static final CommandEntityCleanupManager instance = new CommandEntityCleanupManager();

    public static CommandEntityCleanupManager getInstance() {
        return instance;
    }

    private CommandEntityCleanupManager() {
        log.info("Start initializing " + getClass().getSimpleName());
        Calendar calendar = new GregorianCalendar();
        Date mCommandEntityCleanupTime = Config.<DateTime> getValue(ConfigValues.CommandEntityCleanupTime);
        calendar.setTimeInMillis(mCommandEntityCleanupTime.getTime());

        String cronExpression = String.format("%d %d %d * * ?", calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY));

        log.info("Setting command entity cleanup manager to run at: " + cronExpression);
        SchedulerUtilQuartzImpl.getInstance().scheduleACronJob(this, "onTimer", new Class[] {}, new Object[] {},
                cronExpression);
        log.info("Finished initializing " + getClass().getSimpleName());
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        try {
            log.info("Start deleteAgedOutCommandEntities");
            DateTime latestTimeToKeep = DateTime.getNow().addDays(
                    Config.<Integer>getValue(ConfigValues.CommandEntityAgingThreshold)
                            * -1);
            CommandCoordinatorUtil.removeAllCommandsBeforeDate(latestTimeToKeep);
            log.info("Finished deleteAgedOutCommandEntities");
        } catch (RuntimeException e) {
            log.error("deleteAgedOutCommandEntities failed with exception", e);
        }
    }

}
