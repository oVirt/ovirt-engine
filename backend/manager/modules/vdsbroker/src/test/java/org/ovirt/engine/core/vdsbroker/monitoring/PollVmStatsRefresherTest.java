package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Connecting;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Down;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Error;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Initializing;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.InstallFailed;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Installing;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.InstallingOS;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Kdumping;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Maintenance;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.NonOperational;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.NonResponsive;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.PendingApproval;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.PreparingForMaintenance;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Reboot;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Unassigned;
import static org.ovirt.engine.core.common.businessentities.VDSStatus.Up;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@ExtendWith(MockConfigExtension.class)
public class PollVmStatsRefresherTest {

    private PollVmStatsRefresher underTest;

    @Mock
    private VdsManager vdsManager;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.VdsRefreshRate, 2L),
                MockConfigDescriptor.of(ConfigValues.NumberVmRefreshesBeforeSave, 1)
        );
    }

    public static Stream<Arguments> monitoringNeededByStatus() {
        return Stream.of(
                // host status              is monitoring needed
                Arguments.of(Up,                       true),
                Arguments.of(NonResponsive,            false),
                Arguments.of(Error,                    true),
                Arguments.of(NonOperational,           true),
                Arguments.of(PreparingForMaintenance,  true),
                Arguments.of(Initializing,             false),
                Arguments.of(Connecting,               false),
                Arguments.of(Unassigned,               false),
                Arguments.of(Down,                     false),
                Arguments.of(Maintenance,              false),
                Arguments.of(Installing,               false),
                Arguments.of(InstallFailed,            false),
                Arguments.of(Reboot,                   false),
                Arguments.of(PendingApproval,          false),
                Arguments.of(InstallingOS,             false),
                Arguments.of(Kdumping,                 false)
        );
    }

    @BeforeEach
    public void setup() {
        initMocks(this);
        underTest = spy(new PollVmStatsRefresher(vdsManager));
    }

    @ParameterizedTest
    @MethodSource
    public void monitoringNeededByStatus(VDSStatus status, boolean monitoringNeeded) {
        assertEquals(underTest.isMonitoringNeeded(status), monitoringNeeded);
    }

}
