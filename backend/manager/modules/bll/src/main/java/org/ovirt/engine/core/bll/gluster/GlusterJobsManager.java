package org.ovirt.engine.core.bll.gluster;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GlusterJobsManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(GlusterJobsManager.class);

    @Inject
    private SchedulerUtilQuartzImpl scheduler;

    @Inject
    private Instance<GlusterJob> jobs;

    @PostConstruct
    public void init() {
        if (!glusterModeSupported()) {
            log.debug("Gluster mode not supported. Will not schedule jobs for refreshing Gluster data.");
            return;
        }

        log.debug("Initializing Gluster Jobs Manager");

        for (GlusterJob job : jobs) {
            job.getSchedulingDetails().forEach(j -> scheduler.scheduleAFixedDelayJob(
                    job, j.getMethodName(), new Class[0], new Class[0], j.getDelay(), j.getDelay(), TimeUnit.SECONDS
            ));
        }
    }

    private static boolean glusterModeSupported() {
        Integer appMode = Config.<Integer> getValue(ConfigValues.ApplicationMode);
        return (appMode & ApplicationMode.GlusterOnly.getValue()) > 0;
    }
}
