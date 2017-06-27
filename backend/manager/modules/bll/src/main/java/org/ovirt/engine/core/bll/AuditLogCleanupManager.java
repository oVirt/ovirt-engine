package org.ovirt.engine.core.bll;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.EngineCronTrigger;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuditLogCleanupManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogCleanupManager.class);

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private AuditLogDao auditLogDao;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Calendar calendar = new GregorianCalendar();
        Date auditLogCleanupTime = Config.<DateTime> getValue(ConfigValues.AuditLogCleanupTime);
        calendar.setTimeInMillis(auditLogCleanupTime.getTime());

        String cronExpression = String.format("%d %d %d * * ?", calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY));

        log.info("Setting audit cleanup manager to run at '{}'", cronExpression);
        executor.schedule(this::cleanup, new EngineCronTrigger(cronExpression));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    public void cleanup() {
        try {
            log.debug("Start cleanup");
            DateTime latestTimeToKeep = DateTime.getNow().addDays(
                    Config.<Integer>getValue(ConfigValues.AuditLogAgingThreshold)
                            * -1);
            auditLogDao.removeAllBeforeDate(latestTimeToKeep);
            log.debug("Finished cleanup");
        } catch (Throwable t) {
            log.error("Exception in performing audit log cleanup: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

}
