package org.ovirt.engine.core.bll.dwh;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.dwh.DwhHistoryTimekeepingDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job notifies DWH, that engine is up and running
 */
@Singleton
public class DwhHeartBeat implements BackendService {
    /**
     * Name of method to execute periodically
     */
    private static final String DWH_HEART_BEAT_METHOD = "engineIsRunningNotification";

    /**
     * Logger instance
     */
    private static final Logger log = LoggerFactory.getLogger(DwhHeartBeat.class);

    /**
     * Instance of heartBeat variable
     */
    private DwhHistoryTimekeeping heartBeatVar;
    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;
    @Inject
    private DwhHistoryTimekeepingDao dwhHistoryTimekeepingDao;

    /**
     * Update {@code dwh_history_timekeeping} table to notify DWH, that engine is up an running
     */
    @OnTimerMethodAnnotation(DWH_HEART_BEAT_METHOD)
    public void engineIsRunningNotification() {
        try {
            heartBeatVar.setDateTime(new Date());
            dwhHistoryTimekeepingDao.save(heartBeatVar);
        } catch (Exception ex) {
            log.error("Error updating DWH Heart Beat: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
    }

    /**
     * Starts up DWH Heart Beat as a periodic job
     */
    @PostConstruct
    private void init() {
        log.info("Initializing DWH Heart Beat");
        heartBeatVar = new DwhHistoryTimekeeping();
        heartBeatVar.setVariable(DwhHistoryTimekeepingVariable.HEART_BEAT);

        schedulerUtil.scheduleAFixedDelayJob(this,
                DWH_HEART_BEAT_METHOD,
                new Class[] {},
                new Object[] {},
                0,
                Config.<Integer>getValue(ConfigValues.DwhHeartBeatInterval),
                TimeUnit.SECONDS);
        log.info("DWH Heart Beat initialized");
    }
}
