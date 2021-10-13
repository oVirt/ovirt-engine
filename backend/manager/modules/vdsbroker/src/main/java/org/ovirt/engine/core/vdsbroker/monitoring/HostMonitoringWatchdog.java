package org.ovirt.engine.core.vdsbroker.monitoring;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostMonitoringWatchdog {
    private static final Logger log = LoggerFactory.getLogger(HostMonitoringWatchdog.class);

    private final Supplier<Map<Guid, VdsManager>> vdsManagersDictSupplier;

    private Supplier<Integer> intervalSupplier =
            () -> Config.<Integer> getValue(ConfigValues.HostMonitoringWatchdogIntervalInSeconds);
    private Supplier<Integer> warningThresholdSupplier =
            () -> Config.<Integer> getValue(ConfigValues.HostMonitoringWatchdogInactivityThresholdInSeconds);

    private final ManagedScheduledExecutorService executor;

    private final VdsDao vdsDao;

    private Clock clock = Clock.systemDefaultZone();

    public HostMonitoringWatchdog(ManagedScheduledExecutorService executor,
            VdsDao vdsDao,
            Supplier<Map<Guid, VdsManager>> vdsManagersDictSupplier) {
        this.executor = executor;
        this.vdsDao = vdsDao;
        this.vdsManagersDictSupplier = vdsManagersDictSupplier;
    }

    public void start() {
        int checkIntervalInSeconds = intervalSupplier.get();

        if (checkIntervalInSeconds > 0) {
            int inactivityWarningPeriodInSeconds = warningThresholdSupplier.get();

            log.info("Starting Host Monitoring watchdog service. Check interval: {}s, warning threshold {}s",
                    checkIntervalInSeconds,
                    inactivityWarningPeriodInSeconds);

            executor.scheduleWithFixedDelay(this::monitor,
                    checkIntervalInSeconds,
                    checkIntervalInSeconds,
                    TimeUnit.SECONDS);

        } else {
            log.info(
                    "Host monitoring watchdog service deactivated. " +
                            "To enable set configuration property HOST_MONITORING_WATCHDOG_INTERVAL_IN_SECONDS" +
                            " to value greater than zero");
        }
    }

    // visible for testing
    void monitor() {
        try {
            var currentMillis = clock.instant().toEpochMilli();
            log.debug("Starting host monitoring checks");

            var hostById =
                    vdsDao.getAll()
                            .stream()
                            .filter(host -> host.getVdsType() != VDSType.oVirtNode)
                            .collect(Collectors.toMap(VDS::getId, Function.identity()));

            vdsManagersDictSupplier.get().forEach(detectHostWithStalledHostMonitoring(currentMillis, hostById));

        } catch (Exception e) {
            log.error("Error while host monitoring watchdog check iteration. {}",
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Error while host monitoring watchdog check iteration. Details: ", e);
        }
    }

    private BiConsumer<Guid, VdsManager> detectHostWithStalledHostMonitoring(long currentMillis,
            Map<Guid, VDS> hostById) {
        return (vdsId, vdsManager) -> {
            if (log.isDebugEnabled()) {
                LocalDateTime lastUpdate = toLocalDateTime(vdsManager.getLastUpdate());
                log.debug("Checking host monitoring for {}, last update {}",
                        vdsManager.getVdsHostname(),
                        lastUpdate);
            }
            final VDS host = hostById.get(vdsManager.getVdsId());
            if (host == null) {
                // this situation should never happen but if it happens that will mean
                // there is inconsistency between engine's runtime and db
                log.warn("Could not find host {}[{}] in db.", vdsManager.getVdsHostname(), vdsManager.getVdsId());
                return;
            }
            long inactivityMillis = currentMillis - vdsManager.getLastUpdate();

            if (inactivityMillis >= warningThresholdSupplier.get() * 1000L
                    && host.getStatus().isEligibleForHostMonitoring()) {
                alertLongInactiveHost(vdsManager, inactivityMillis);
            }
        };
    }

    private static LocalDateTime toLocalDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // visible for testing
    void alertLongInactiveHost(VdsManager vdsManager, long inactivityMillis) {
        log.warn("Monitoring not executed for the host {} [{}] for {}ms",
                vdsManager.getVdsHostname(),
                vdsManager.getVdsId(),
                inactivityMillis);
    }

    // visible for testing
    void setClock(Clock clock) {
        this.clock = clock;
    }

    // visible for testing
    void setHostMonitoringIntervalConfigSupplier(Supplier<Integer> intervalSupplier) {
        this.intervalSupplier = intervalSupplier;
    }

    // visible for testing
    void setHostMonitoringWatchdogWarningThresholdSupplier(Supplier<Integer> warningThresholdSupplier) {
        this.warningThresholdSupplier = warningThresholdSupplier;
    }
}
