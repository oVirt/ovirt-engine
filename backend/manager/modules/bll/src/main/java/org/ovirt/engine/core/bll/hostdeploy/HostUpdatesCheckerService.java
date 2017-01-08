package org.ovirt.engine.core.bll.hostdeploy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpdatesCheckerService implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(HostUpdatesCheckerService.class);

    @Inject
    private SchedulerUtilQuartzImpl scheduler;

    @Inject
    private VdsDao hostDao;

    @Inject
    private HostUpdatesChecker hostUpdatesChecker;

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(Config.<Integer> getValue(ConfigValues.HostCheckForUpdatesThreadPoolSize));

    @PostConstruct
    public void scheduleJob() {
        double availableUpdatesRefreshRate = Config.<Double> getValue(ConfigValues.HostPackagesUpdateTimeInHours);
        if (availableUpdatesRefreshRate > 0) {
            final int HOURS_TO_MINUTES = 60;
            long rateInMinutes = Math.round(availableUpdatesRefreshRate * HOURS_TO_MINUTES);

            scheduler.scheduleAFixedDelayJob(
                    this,
                    "availableUpdates",
                    new Class[0],
                    new Object[0],
                    15,
                    rateInMinutes,
                    TimeUnit.MINUTES);
        }
    }

    @OnTimerMethodAnnotation("availableUpdates")
    public void availableUpdates() {
        hostDao.getAll()
                .stream()
                .filter(h -> h.getStatus().isEligibleForCheckUpdates())
                .forEach(this::submitCheckUpdatesForHost);
    }

    private void submitCheckUpdatesForHost(VDS host) {
        try {
            executor.submit(() -> hostUpdatesChecker.checkForUpdates(host));
        } catch (RejectedExecutionException ex) {
            log.error("Failed to submit check-updates task to executor service, host '{}' will be skipped",
                    host.getName());
        }
    }
}
