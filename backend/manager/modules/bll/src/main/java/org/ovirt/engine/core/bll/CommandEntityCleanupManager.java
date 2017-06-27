package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.EngineCronTrigger;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CommandEntityCleanupManager implements BackendService {

    private static Logger log = LoggerFactory.getLogger(CommandEntityCleanupManager.class);

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Calendar calendar = new GregorianCalendar();
        Date commandEntityCleanupTime = Config.<DateTime> getValue(ConfigValues.CommandEntityCleanupTime);
        calendar.setTimeInMillis(commandEntityCleanupTime.getTime());

        String cronExpression = String.format("%d %d %d * * ?", calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY));

        log.info("Setting command entity cleanup manager to run at: {}", cronExpression);
        executor.schedule(this::cleanup, new EngineCronTrigger(cronExpression));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void cleanup() {
        try {
            log.debug("Start cleanup");
            DateTime latestTimeToKeep = DateTime.getNow().addDays(
                    Config.<Integer>getValue(ConfigValues.CommandEntityAgingThreshold)
                            * -1);
            commandCoordinatorUtil.removeAllCommandsBeforeDate(latestTimeToKeep);
            log.debug("Finished cleanup");
        } catch (Throwable t) {
            log.error("Exception in performing command entity cleanup: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

}
