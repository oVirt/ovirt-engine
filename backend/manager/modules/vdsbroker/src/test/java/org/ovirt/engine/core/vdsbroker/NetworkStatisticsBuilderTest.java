package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Parameterized.class)
public class NetworkStatisticsBuilderTest {

    @ClassRule
    public static MockConfigRule mockConfig = new MockConfigRule(
            mockConfig(ConfigValues.TotalNetworkStatisticsReported, Version.v3_5.toString(), false),
            mockConfig(ConfigValues.TotalNetworkStatisticsReported, Version.v3_6.toString(), true));

    private final NetworkStatisticsBuilder statsBuilder;
    private final NetworkInterface<NetworkStatistics> existingIface;
    private final NetworkInterface<NetworkStatistics> reportedIface;

    private final Double expectedRxDrops;
    private final Double expectedRxRate;
    private final Long expectedRxTotal;
    private final Long expectedRxOffset;
    private final Double expectedTxDrops;
    private final Double expectedTxRate;
    private final Long expectedTxTotal;
    private final Long expectedTxOffset;
    private final Double expectedTime;
    private final Integer expectedSpeed;

    public NetworkStatisticsBuilderTest(Version version,
            Double previousRxDrops,
            Double previousRxRate,
            Long previousRxTotal,
            Long previousRxOffset,
            Double previousTxDrops,
            Double previousTxRate,
            Long previousTxTotal,
            Long previousTxOffset,
            Double previousTime,
            Integer previousSpeed,
            Double reportedRxDrops,
            Double reportedRxRate,
            Long reportedRxTotal,
            Double reportedTxDrops,
            Double reportedTxRate,
            Long reportedTxTotal,
            Double reportedTime,
            Integer reportedSpeed,
            Double expectedRxDrops,
            Double expectedRxRate,
            Long expectedRxTotal,
            Long expectedRxOffset,
            Double expectedTxDrops,
            Double expectedTxRate,
            Long expectedTxTotal,
            Long expectedTxOffset,
            Double expectedTime,
            Integer expectedSpeed) {

        statsBuilder = new NetworkStatisticsBuilder(version);

        existingIface =
                constructInterface(previousRxDrops,
                        previousRxRate,
                        previousRxTotal,
                        previousRxOffset,
                        previousTxDrops,
                        previousTxRate,
                        previousTxTotal,
                        previousTxOffset,
                        previousTime,
                        previousSpeed);

        reportedIface =
                constructInterface(reportedRxDrops,
                        reportedRxRate,
                        reportedRxTotal,
                        null,
                        reportedTxDrops,
                        reportedTxRate,
                        reportedTxTotal,
                        null,
                        reportedTime,
                        reportedSpeed);

        this.expectedRxDrops = expectedRxDrops;
        this.expectedRxRate = expectedRxRate;
        this.expectedRxTotal = expectedRxTotal;
        this.expectedRxOffset = expectedRxOffset;
        this.expectedTxDrops = expectedTxDrops;
        this.expectedTxRate = expectedTxRate;
        this.expectedTxTotal = expectedTxTotal;
        this.expectedTxOffset = expectedTxOffset;
        this.expectedTime = expectedTime;
        this.expectedSpeed = expectedSpeed;
    }

    @Test
    public void verifyResult() {
        statsBuilder.updateExistingInterfaceStatistics(existingIface, reportedIface);
        NetworkStatistics existingStats = existingIface.getStatistics();
        assertEquals(expectedRxDrops, existingStats.getReceiveDropRate());
        assertEquals(expectedRxRate, existingStats.getReceiveRate());
        assertEquals(expectedRxTotal, existingStats.getReceivedBytes());
        assertEquals(expectedRxOffset, existingStats.getReceivedBytesOffset());
        assertEquals(expectedTxDrops, existingStats.getTransmitDropRate());
        assertEquals(expectedTxRate, existingStats.getTransmitRate());
        assertEquals(expectedTxTotal, existingStats.getTransmittedBytes());
        assertEquals(expectedTxOffset, existingStats.getTransmittedBytesOffset());
        assertEquals(expectedTime, existingStats.getSampleTime());
        assertEquals(expectedSpeed, existingIface.getSpeed());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {

                // total stats not supported - make sure they're reset and reported rate is taken
                { Version.v3_5,
                    anyDouble(),  anyDouble(), anyLong(), anyLong(), anyDouble(),  anyDouble(), anyLong(), anyLong(), 0D,          anyInt(),
                    100D,         10D,         anyLong(),            100D,         10D,         anyLong(),            anyDouble(), 1000,
                    100D,         10D,         null,      null,      100D,         10D,         null,      null,      null       , 1000
                },

                // everything's supported and reported, and rate should be 100Mbps (10%)
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        1D, 1000,
                    100D,         10D,         25000000L, 1000L, 100D,         10D,         25000000L, 1000L, 1D, 1000
                },

                // RX total wasn't reported - RX total and rate should be set to null
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), null,             100D,         anyDouble(), 24999000L,        1D, 1000,
                    100D,         null,        null,      1000L, 100D,         10D,         25000000L, 1000L, 1D, 1000
                },

                // TX total wasn't reported - TX total and rate should be set to null
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), null,             1D, 1000,
                    100D,         10D,         25000000L, 1000L, 100D,         null,        null,      1000L, 1D, 1000
                },

                // RX offset wasn't previously set - should be set so that total RX is zero, rate irrelevant
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, null,       anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 25000000L,             100D,         anyDouble(), 24999000L,        1D, 1000,
                    100D,         null,        0L,        -25000000L, 100D,         10D,         25000000L, 1000L, 1D, 1000
                },

                // TX offset wasn't previously set - should be set so that total TX is zero, rate irrelevant
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, null,       0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 25000000L,             1D, 1000,
                    100D,         10D,         25000000L, 1000L, 100D,         null,        0L,        -25000000L, 1D, 1000
                },

                // RX total wrapped around - offset should be updated
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 17500000L, 1000L,     anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 12500000L,            100D,         anyDouble(), 24999000L,        1D, 1000,
                    100D,         10D,         30000000L, 17500000L, 100D,         10D,         25000000L, 1000L, 1D, 1000
                },

                // TX total wrapped around - offset should be updated
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 17500000L, 1000L,     0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 12500000L,            1D, 1000,
                    100D,         10D,         25000000L, 1000L, 100D,         10D,         30000000L, 17500000L, 1D, 1000
                },

                // current time measurement is missing - rates shouldn't be computed
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D,     anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        null,   1000,
                    100D,         null,        25000000L, 1000L, 100D,         null,        25000000L, 1000L, null,   1000
                },

                // previous time measurement is missing - rates shouldn't be computed
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, null, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        1D,   1000,
                    100D,         null,        25000000L, 1000L, 100D,         null,        25000000L, 1000L, 1D,   1000
                },

                // time measurement decreased - rates shouldn't be computed
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 1D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        0D, 1000,
                    100D,         null,        25000000L, 1000L, 100D,         null,        25000000L, 1000L, 0D, 1000
                },

                // speed is missing - rates shouldn't be computed
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        1D, null,
                    100D,         null,        25000000L, 1000L, 100D,         null,        25000000L, 1000L, 1D, null
                },

                // speed is reported as zero - rates shouldn't be computed
                { Version.v3_6,
                    anyDouble(),  anyDouble(), 12500000L, 1000L, anyDouble(),  anyDouble(), 12500000L, 1000L, 0D, anyInt(),
                    100D,         anyDouble(), 24999000L,        100D,         anyDouble(), 24999000L,        1D, 0,
                    100D,         null,        25000000L, 1000L, 100D,         null,        25000000L, 1000L, 1D, 0
                }
        });
    }

    private static double anyDouble() {
        return 100 * RandomUtils.nextDouble();
    }

    private static int anyInt() {
        return RandomUtils.nextInt();
    }

    private static long anyLong() {
        return RandomUtils.nextLong();
    }

    private static NetworkInterface<NetworkStatistics> constructInterface(Double rxDrops,
            Double rxRate,
            Long rxTotal,
            Long rxOffset,
            Double txDrops,
            Double txRate,
            Long txTotal,
            Long txOffset,
            Double sampleTime,
            Integer speed) {

        NetworkInterface<NetworkStatistics> iface = new TestableNetworkInterface();
        NetworkStatistics stats = new TestableNetworkStatistics();
        iface.setStatistics(stats);

        stats.setReceiveDropRate(rxDrops);
        stats.setReceiveRate(rxRate);
        stats.setReceivedBytes(rxTotal);
        stats.setReceivedBytesOffset(rxOffset);
        stats.setTransmitDropRate(txDrops);
        stats.setTransmitRate(txRate);
        stats.setTransmittedBytes(txTotal);
        stats.setTransmittedBytesOffset(txOffset);
        stats.setSampleTime(sampleTime);
        iface.setSpeed(speed);

        return iface;
    }

    private static class TestableNetworkInterface extends NetworkInterface<NetworkStatistics> {
        private static final long serialVersionUID = 3479879927558781006L;
    }

    private static class TestableNetworkStatistics extends NetworkStatistics {
        private static final long serialVersionUID = -368536092676195302L;
    }

}
