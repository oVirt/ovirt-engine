package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.vdsbroker.VdsManager;

class HostMonitoringWatchdogTest {

    private final String host1 = "host1";
    private final Guid hostId1 = Guid.createGuidFromString("00000000-0000-0000-0000-000000001111");
    private final String host2 = "host2";
    private final Guid hostId2 = Guid.createGuidFromString("00000000-0000-0000-0000-000000002222");
    private final Instant currentTime = Instant.now();

    @Mock
    private VdsDao dao;

    @Mock
    private Clock clock;

    @Mock
    private VdsManager vdsManager1;

    @Mock
    private VdsManager vdsManager2;

    @Mock
    private VDS vds1;

    @Mock
    private VDS vds2;

    @Mock
    private ManagedScheduledExecutorService executor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        given(clock.instant()).willReturn(currentTime);

        given(vdsManager1.getVdsId()).willReturn(hostId1);
        given(vdsManager1.getVdsHostname()).willReturn(host1);
        given(vds1.getId()).willReturn(hostId1);
        given(vds1.getHostName()).willReturn(host1);
        given(vds1.getStatus()).willReturn(VDSStatus.Up);

        given(vdsManager2.getVdsId()).willReturn(hostId2);
        given(vdsManager2.getVdsHostname()).willReturn(host2);
        given(vds2.getId()).willReturn(hostId2);
        given(vds2.getHostName()).willReturn(host2);
        given(vds2.getStatus()).willReturn(VDSStatus.Up);
    }

    public HostMonitoringWatchdog createWatchdog(int intervalInSec,
            int warningThresholdInSec,
            Map<Guid, VdsManager> vdsManagerDict) {
        HostMonitoringWatchdog watchdog = Mockito.spy(new HostMonitoringWatchdog(executor, dao, () -> vdsManagerDict));
        watchdog.setClock(clock);
        watchdog.setHostMonitoringIntervalConfigSupplier(() -> intervalInSec);
        watchdog.setHostMonitoringWatchdogWarningThresholdSupplier(() -> warningThresholdInSec);
        return watchdog;
    }

    public HostMonitoringWatchdog createWatchdog(Map<Guid, VdsManager> vdsManagerDict) {
        return createWatchdog(1, 10, vdsManagerDict);
    }

    @Test
    public void shouldNotEnableWatchdogWhenDeactivatedViaProperty() {
        HostMonitoringWatchdog watchdog = createWatchdog(0, 10, Collections.emptyMap());

        watchdog.start();

        Mockito.verifyNoInteractions(executor);
    }

    @Test
    public void shouldEnableWatchdogUsingConfigurationProperties() {
        HostMonitoringWatchdog watchdog = createWatchdog(Collections.emptyMap());

        watchdog.start();

        verify(executor).scheduleWithFixedDelay(any(Runnable.class), eq(1L), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void shouldWarnWhenHostInactivityReachesThreshold() {
        // given
        HostMonitoringWatchdog watchdog = createWatchdog(Map.of(hostId1, vdsManager1, hostId2, vdsManager2));
        given(vdsManager1.getLastUpdate()).willReturn(currentTime.toEpochMilli() - 20000);
        given(vdsManager2.getLastUpdate()).willReturn(currentTime.toEpochMilli() - 3000);
        given(dao.getAll()).willReturn(List.of(vds1, vds2));

        watchdog.start();

        // when
        watchdog.monitor();

        // then
        verify(watchdog).alertLongInactiveHost(eq(vdsManager1), anyLong());
    }

    @ParameterizedTest
    @MethodSource("hostStatusesEligibleForHostMonitoring")
    public void shouldOnlyWarnAboutInactivityOfHostsWithSelectedStatuses(VDSStatus status,
            boolean eligibleForMonitoring) {
        // given
        HostMonitoringWatchdog watchdog = createWatchdog(Map.of(hostId1, vdsManager1));
        given(vdsManager1.getLastUpdate()).willReturn(currentTime.toEpochMilli() - 20000);
        given(vds1.getStatus()).willReturn(status);
        given(dao.getAll()).willReturn(List.of(vds1));

        watchdog.start();

        // when
        watchdog.monitor();

        // then
        if (eligibleForMonitoring) {
            verify(watchdog).alertLongInactiveHost(eq(vdsManager1), anyLong());
        } else {
            verify(watchdog, never()).alertLongInactiveHost(any(VdsManager.class), anyLong());
        }

    }

    public static Stream<Arguments> hostStatusesEligibleForHostMonitoring() {
        Set<VDSStatus> ineligibleForMonitoring = new HashSet<>();
        ineligibleForMonitoring.add(VDSStatus.Installing);
        ineligibleForMonitoring.add(VDSStatus.InstallFailed);
        ineligibleForMonitoring.add(VDSStatus.Reboot);
        ineligibleForMonitoring.add(VDSStatus.Maintenance);
        ineligibleForMonitoring.add(VDSStatus.PendingApproval);
        ineligibleForMonitoring.add(VDSStatus.InstallingOS);
        ineligibleForMonitoring.add(VDSStatus.Down);
        ineligibleForMonitoring.add(VDSStatus.Kdumping);

        return Arrays.stream(VDSStatus.values())
                .map(status -> Arguments.of(status, !ineligibleForMonitoring.contains(status)));
    }
}
