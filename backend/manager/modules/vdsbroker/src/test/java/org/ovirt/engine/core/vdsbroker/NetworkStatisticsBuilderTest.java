package org.ovirt.engine.core.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.utils.RandomUtils;

public class NetworkStatisticsBuilderTest {
    @ParameterizedTest
    @MethodSource
    public void buildStatistics(Double previousRxDrops,
            Double previousRxRate,
            BigInteger previousRxTotal,
            BigInteger previousRxOffset,
            Double previousTxDrops,
            Double previousTxRate,
            BigInteger previousTxTotal,
            BigInteger previousTxOffset,
            Double previousTime,
            Integer previousSpeed,
            Double reportedRxDrops,
            Double reportedRxRate,
            BigInteger reportedRxTotal,
            Double reportedTxDrops,
            Double reportedTxRate,
            BigInteger reportedTxTotal,
            Double reportedTime,
            Integer reportedSpeed,
            Double expectedRxDrops,
            Double expectedRxRate,
            BigInteger expectedRxTotal,
            BigInteger expectedRxOffset,
            Double expectedTxDrops,
            Double expectedTxRate,
            BigInteger expectedTxTotal,
            BigInteger expectedTxOffset,
            Double expectedTime,
            Integer expectedSpeed) {

        NetworkStatisticsBuilder statsBuilder = new NetworkStatisticsBuilder();

        NetworkInterface<NetworkStatistics> existingIface =
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

        NetworkInterface<NetworkStatistics> reportedIface =
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

    public static Stream<Arguments> buildStatistics() {
        final BigInteger bi12500000 = new BigInteger("12500000");
        final BigInteger bi1000 = new BigInteger("1000");
        final BigInteger bi24999000 = new BigInteger("24999000");
        final BigInteger bi25000000 = new BigInteger("25000000");
        final BigInteger bi17500000 = new BigInteger("17500000");
        final BigInteger bi30000000 = new BigInteger("30000000");
        final BigInteger bi0 = BigInteger.ZERO;
        return Stream.of(

            // everything's supported and reported, and rate should be 100Mbps (10%)
            Arguments.of(
                //rxDrop      rxRate       rxTotal     rxOffset txDrop      txRate       txTotal     txOffset time speed
                anyDouble(),  anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,         anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  1D,         1000,
                100D,         10D,         bi25000000, bi1000, 100D,        10D,         bi25000000, bi1000, 1D, 1000
            ),

            // RX total wasn't reported - RX total and rate should be set to null
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), null,       100D,   anyDouble(), bi24999000,  1D,         1000,
                100D,        null,        null,       bi1000, 100D,        10D,         bi25000000, bi1000, 1D, 1000
            ),

            // TX total wasn't reported - TX total and rate should be set to null
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), null,        1D,         1000,
                100D,        10D,         bi25000000, bi1000, 100D,        null,        null,       bi1000, 1D, 1000
            ),

            // RX offset wasn't previously set - should be set so that total RX is zero, rate irrelevant
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, null,                anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), bi25000000, 100D,                anyDouble(), bi24999000,  1D,         1000,
                100D,        null,        bi0,        bi25000000.negate(), 100D,        10D,         bi25000000, bi1000, 1D, 1000
            ),

            // TX offset wasn't previously set - should be set so that total TX is zero, rate irrelevant
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, null,                0D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi25000000,  1D,         1000,
                100D,        10D,         bi25000000, bi1000, 100D,        null,        bi0,        bi25000000.negate(), 1D, 1000
            ),

            // RX total wrapped around - offset should be updated
            Arguments.of(
                anyDouble(), anyDouble(), bi17500000, bi1000,     anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), bi12500000, 100D,       anyDouble(), bi24999000,  1D,         1000,
                100D,        10D,         bi30000000, bi17500000, 100D,        10D,         bi25000000, bi1000, 1D, 1000
            ),

            // TX total wrapped around - offset should be updated
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi17500000, bi1000,     0D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi12500000,  1D,         1000,
                100D,        10D,         bi25000000, bi1000, 100D,        10D,         bi30000000, bi17500000, 1D, 1000
            ),

            // current time measurement is missing - rates shouldn't be computed
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D,   anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  null,       1000,
                100D,        null,        bi25000000, bi1000, 100D,        null,        bi25000000, bi1000, null, 1000
            ),

            // previous time measurement is missing - rates shouldn't be computed
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, null, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  1D,         1000,
                100D,        null,        bi25000000, bi1000, 100D,        null,        bi25000000, bi1000, 1D,   1000
            ),

            // time measurement decreased - rates shouldn't be computed
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 1D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  0D,         1000,
                100D,        null,        bi25000000, bi1000, 100D,        null,        bi25000000, bi1000, 0D, 1000
            ),

            // speed is missing - rates shouldn't be computed
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  1D,         null,
                100D,        null,        bi25000000, bi1000, 100D,        null,        bi25000000, bi1000, 1D, null
            ),

            // speed is reported as zero - rates shouldn't be computed
            Arguments.of(
                anyDouble(), anyDouble(), bi12500000, bi1000, anyDouble(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                100D,        anyDouble(), bi24999000, 100D,   anyDouble(), bi24999000,  1D,         0,
                100D,        null,        bi25000000, bi1000, 100D,        null,        bi25000000, bi1000, 1D, 0
            )
        );
    }

    private static double anyDouble() {
        return 100 * RandomUtils.instance().nextDouble();
    }

    private static int anyInt() {
        return RandomUtils.instance().nextInt();
    }

    private static NetworkInterface<NetworkStatistics> constructInterface(Double rxDrops,
            Double rxRate,
            BigInteger rxTotal,
            BigInteger rxOffset,
            Double txDrops,
            Double txRate,
            BigInteger txTotal,
            BigInteger txOffset,
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

        @Override
        public Object getQueryableId() {
            return new Object();
        }
    }

    private static class TestableNetworkStatistics extends NetworkStatistics {
        private static final long serialVersionUID = -368536092676195302L;
    }

}
