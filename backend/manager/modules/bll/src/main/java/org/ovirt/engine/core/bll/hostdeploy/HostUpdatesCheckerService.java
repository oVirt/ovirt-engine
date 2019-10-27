package org.ovirt.engine.core.bll.hostdeploy;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpdatesCheckerService implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(HostUpdatesCheckerService.class);

    @Inject
    private VdsDao hostDao;

    @Inject
    private HostUpdatesChecker hostUpdatesChecker;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.HostUpdatesChecker)
    private ManagedExecutorService executor;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService scheduledExecutor;

    @PostConstruct
    public void scheduleJob() {
        double availableUpdatesRefreshRate = Config.<Double> getValue(ConfigValues.HostPackagesUpdateTimeInHours);
        if (availableUpdatesRefreshRate > 0) {
            final int HOURS_TO_MINUTES = 60;
            long rateInMinutes = Math.round(availableUpdatesRefreshRate * HOURS_TO_MINUTES);

            scheduledExecutor.scheduleWithFixedDelay(this::availableUpdates,
                    15,
                    rateInMinutes,
                    TimeUnit.MINUTES);
        }
    }

    private void availableUpdates() {
        try {
            hostDao.getAll()
                    .stream()
                    .filter(h -> h.getStatus().isEligibleForCheckUpdates() && h.isManaged())
                    .forEach(this::submitCheckUpdatesForHost);
        } catch (Throwable t) {
            log.error("Exception in checking for available updates: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
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
