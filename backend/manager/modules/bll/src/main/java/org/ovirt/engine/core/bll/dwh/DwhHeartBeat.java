package org.ovirt.engine.core.bll.dwh;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.dwh.DwhHistoryTimekeepingDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
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
    private DwhHistoryTimekeepingDao dwhHistoryTimekeepingDao;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    /**
     * Update {@code dwh_history_timekeeping} table to notify DWH, that engine is up an running
     */
    private void engineIsRunningNotification() {
        try {
            log.debug("DWH Heart Beat - Start");
            TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, () -> {
                heartBeatVar.setDateTime(new Date());
                dwhHistoryTimekeepingDao.save(heartBeatVar);
                return null;
            });
            log.debug("DWH Heart Beat - End");
        } catch (Throwable t) {
            log.error("Error updating DWH Heart Beat: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
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

        executor.scheduleWithFixedDelay(this::engineIsRunningNotification,
                0,
                Config.<Long>getValue(ConfigValues.DwhHeartBeatInterval),
                TimeUnit.SECONDS);
        log.info("DWH Heart Beat initialized");
    }
}
