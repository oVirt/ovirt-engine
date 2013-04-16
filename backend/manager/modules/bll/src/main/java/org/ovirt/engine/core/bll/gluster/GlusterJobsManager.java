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
                getGlusterRefreshRateLight(),
                getGlusterRefreshRateLight(),
                TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(GlusterSyncJob.getInstance(),
                "refreshHeavyWeightData",
                new Class[0],
                new Object[0],
                getGlusterRefreshRateHeavy(),
                getGlusterRefreshRateHeavy(),
                TimeUnit.SECONDS);

    }

    private static boolean glusterModeSupported() {
        Integer appMode = Config.<Integer> GetValue(ConfigValues.ApplicationMode);
        return ((appMode & ApplicationMode.GlusterOnly.getValue()) > 0);
    }

    private static int getGlusterRefreshRateLight() {
        return Config.<Integer> GetValue(ConfigValues.GlusterRefreshRateLight);
    }

    private static int getGlusterRefreshRateHeavy() {
        return Config.<Integer> GetValue(ConfigValues.GlusterRefreshRateHeavy);
    }

}
