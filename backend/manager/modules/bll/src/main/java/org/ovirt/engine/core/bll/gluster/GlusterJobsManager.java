package org.ovirt.engine.core.bll.gluster;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class GlusterJobsManager {

    private static final Log log = LogFactory.getLog(GlusterJobsManager.class);

    public static void init() {
        if (!glusterModeSupported()) {
            log.debug("Gluster mode not supported. Will not schedule jobs for refreshing Gluster data.");
            return;
        }

        log.debug("Initializing Gluster Jobs Manager");

        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();

        scheduler.scheduleAFixedDelayJob(GlusterSyncJob.getInstance(),
                "refreshLightWeightData",
                new Class[0],
                new Object[0],
                getRefreshRate(ConfigValues.GlusterRefreshRateLight),
                getRefreshRate(ConfigValues.GlusterRefreshRateLight),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterSyncJob.getInstance(),
                "refreshHeavyWeightData",
                new Class[0],
                new Object[0],
                getRefreshRate(ConfigValues.GlusterRefreshRateHeavy),
                getRefreshRate(ConfigValues.GlusterRefreshRateHeavy),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterHookSyncJob.getInstance(),
                "refreshHooks",
                new Class[0],
                new Object[0],
                getRefreshRate(ConfigValues.GlusterRefreshRateHooks),
                getRefreshRate(ConfigValues.GlusterRefreshRateHooks),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterServiceSyncJob.getInstance(),
                "refreshGlusterServices",
                new Class[0],
                new Object[0],
                getRefreshRate(ConfigValues.GlusterRefreshRateLight),
                getRefreshRate(ConfigValues.GlusterRefreshRateLight),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterTasksSyncJob.getInstance(),
                "gluster_async_task_poll_event",
                new Class[0] ,
                new Class [0],
                getRefreshRate(ConfigValues.GlusterRefreshRateTasks),
                getRefreshRate(ConfigValues.GlusterRefreshRateTasks),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterGeoRepSyncJob.getInstance(),
                "gluster_georep_poll_event",
                new Class[0] ,
                new Class [0],
                getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepDiscoveryInSecs),
                getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepDiscoveryInSecs),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterGeoRepSyncJob.getInstance(),
                "gluster_georepstatus_poll_event",
                new Class[0],
                new Class[0],
                getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepStatusInSecs),
                getRefreshRate(ConfigValues.GlusterRefreshRateGeoRepStatusInSecs),
                TimeUnit.SECONDS);
    }

    private static boolean glusterModeSupported() {
        Integer appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        return ((appMode & ApplicationMode.GlusterOnly.getValue()) > 0);
    }

    private static int getRefreshRate(ConfigValues refreshRateConfig) {
        return Config.<Integer> getValue(refreshRateConfig);
    }

}
