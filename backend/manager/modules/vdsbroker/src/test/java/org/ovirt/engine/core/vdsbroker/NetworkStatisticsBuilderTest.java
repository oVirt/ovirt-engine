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

    static final BigInteger biMaxLong = new BigInteger(String.valueOf(Long.MAX_VALUE));
    static final BigInteger bi12500000 = new BigInteger("12500000");
    static final BigInteger bi1000 = new BigInteger("1000");
    static final BigInteger bi24999000 = new BigInteger("24999000");
    static final BigInteger bi25000000 = new BigInteger("25000000");
    static final BigInteger bi17500000 = new BigInteger("17500000");
    static final BigInteger bi30000000 = new BigInteger("30000000");
    static final BigInteger bi100 = new BigInteger("100");
    static final BigInteger bi0 = BigInteger.ZERO;

    @ParameterizedTest
    @MethodSource({"buildStatistics", "buildStatistics2"})
    public void buildStatistics(BigInteger previousRxDrops,
            Double previousRxRate,
            BigInteger previousRxTotal,
            BigInteger previousRxOffset,
            BigInteger previousTxDrops,
            Double previousTxRate,
            BigInteger previousTxTotal,
            BigInteger previousTxOffset,
            Double previousTime,
            Integer previousSpeed,
            BigInteger reportedRxDrops,
            Double reportedRxRate,
            BigInteger reportedRxTotal,
            BigInteger reportedTxDrops,
            Double reportedTxRate,
            BigInteger reportedTxTotal,
            Double reportedTime,
            Integer reportedSpeed,
            BigInteger expectedRxDrops,
            Double expectedRxRate,
            BigInteger expectedRxTotal,
            BigInteger expectedRxOffset,
            BigInteger expectedTxDrops,
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
        assertEquals(expectedRxDrops, existingStats.getReceiveDrops());
        assertEquals(expectedRxRate, existingStats.getReceiveRate());
        assertEquals(expectedRxTotal, existingStats.getReceivedBytes());
        assertEquals(expectedRxOffset, existingStats.getReceivedBytesOffset());
        assertEquals(expectedTxDrops, existingStats.getTransmitDrops());
        assertEquals(expectedTxRate, existingStats.getTransmitRate());
        assertEquals(expectedTxTotal, existingStats.getTransmittedBytes());
        assertEquals(expectedTxOffset, existingStats.getTransmittedBytesOffset());
        assertEquals(expectedTime, existingStats.getSampleTime());
        assertEquals(expectedSpeed, existingIface.getSpeed());
    }

    public static Stream<Arguments> buildStatistics() {
        return buildArguments(
                bi0,
                bi100,
                bi1000,
                bi12500000,
                bi17500000,
                bi24999000,
                bi25000000,
                bi30000000,
                10D
        );
    }

    public static Stream<Arguments> buildStatistics2() {
        return buildArguments(
                bi0.multiply(biMaxLong),
                bi100.multiply(biMaxLong),
                bi1000.multiply(biMaxLong),
                bi12500000.multiply(biMaxLong),
                bi17500000.multiply(biMaxLong),
                bi24999000.multiply(biMaxLong),
                bi25000000.multiply(biMaxLong),
                bi30000000.multiply(biMaxLong),
                100D
        );
    }


    public static Stream<Arguments> buildArguments(
            BigInteger bi0,
            BigInteger bi100,
            BigInteger bi1000,
            BigInteger bi12500000,
            BigInteger bi17500000,
            BigInteger bi24999000,
            BigInteger bi25000000,
            BigInteger bi30000000,
            Double expectRate
    ) {
        return Stream.of(

            // everything's supported and reported, and rate should be 100Mbps (10%)
            Arguments.of(
                //rxDrop     rxRate       rxTotal     rxOffset txDrop      txRate       txTotal     txOffset time speed
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(), //previous
                bi100,       anyDouble(), bi24999000,         bi100,       anyDouble(), bi24999000,         1D, 1000,     //reported
                bi100,       expectRate,  bi25000000, bi1000, bi100,       expectRate,  bi25000000, bi1000, 1D, 1000      //expected
            ),

            // RX total wasn't reported - RX total and rate should be set to null
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), null,       bi100,               anyDouble(), bi24999000,         1D, 1000,
                bi100,       null,        null,       bi1000, bi100,       expectRate,  bi25000000, bi1000, 1D, 1000
            ),

            // TX total wasn't reported - TX total and rate should be set to null
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), null,               1D, 1000,
                bi100,       expectRate,  bi25000000, bi1000, bi100,       null,        null,       bi1000, 1D, 1000
            ),

            // RX offset wasn't previously set - should be set so that total RX is zero, rate irrelevant
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, null,                 anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), bi25000000, bi100,                             anyDouble(), bi24999000,         1D, 1000,
                bi100,       null,        bi0,        bi25000000.negate(),  bi100,       expectRate,  bi25000000, bi1000, 1D, 1000
            ),

            // TX offset wasn't previously set - should be set so that total TX is zero, rate irrelevant
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, null,                0D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi25000000,                      1D, 1000,
                bi100,       expectRate,   bi25000000, bi1000, bi100,       null,        bi0,        bi25000000.negate(), 1D, 1000
            ),

            // RX total wrapped around - offset should be updated
            Arguments.of(
                anyBigInt(), anyDouble(), bi17500000, bi1000,     anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), bi12500000, bi100,                   anyDouble(), bi24999000,         1D, 1000,
                bi100,       expectRate,  bi30000000, bi17500000, bi100,       expectRate,  bi25000000, bi1000, 1D, 1000
            ),

            // TX total wrapped around - offset should be updated
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi17500000, bi1000,     0D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi12500000,             1D, 1000,
                bi100,       expectRate,  bi25000000, bi1000, bi100,       expectRate,  bi30000000, bi17500000, 1D, 1000
            ),

            // current time measurement is missing - rates shouldn't be computed
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D,   anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi24999000,         null, 1000,
                bi100,       null,        bi25000000, bi1000, bi100,       null,        bi25000000, bi1000, null, 1000
            ),

            // previous time measurement is missing - rates shouldn't be computed
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, null, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi24999000,         1D,   1000,
                bi100,       null,        bi25000000, bi1000, bi100,       null,        bi25000000, bi1000, 1D,   1000
            ),

            // time measurement decreased - rates shouldn't be computed
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 1D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi24999000,         0D, 1000,
                bi100,       null,        bi25000000, bi1000, bi100,       null,        bi25000000, bi1000, 0D, 1000
            ),

            // speed is missing - rates shouldn't be computed
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,  anyDouble(), bi24999000,  1D,         null,
                bi100,       null,        bi25000000, bi1000, bi100,       null,        bi25000000, bi1000, 1D, null
            ),

            // speed is reported as zero - rates shouldn't be computed
            Arguments.of(
                anyBigInt(), anyDouble(), bi12500000, bi1000, anyBigInt(), anyDouble(), bi12500000, bi1000, 0D, anyInt(),
                bi100,       anyDouble(), bi24999000, bi100,               anyDouble(), bi24999000,         1D, 0,
                bi100,       null,        bi25000000, bi1000, bi100,       null,        bi25000000, bi1000, 1D, 0
            )
        );
    }

    private static double anyDouble() {
        return 100 * RandomUtils.instance().nextDouble();
    }

    private static int anyInt() {
        return RandomUtils.instance().nextInt();
    }

    private static BigInteger anyBigInt() {
        return new BigInteger(String.valueOf(anyInt())).multiply(biMaxLong);
    }

    private static NetworkInterface<NetworkStatistics> constructInterface(BigInteger rxDrops,
            Double rxRate,
            BigInteger rxTotal,
            BigInteger rxOffset,
            BigInteger txDrops,
            Double txRate,
            BigInteger txTotal,
            BigInteger txOffset,
            Double sampleTime,
            Integer speed) {

        NetworkInterface<NetworkStatistics> iface = new TestableNetworkInterface();
        NetworkStatistics stats = new TestableNetworkStatistics();
        iface.setStatistics(stats);

        stats.setReceiveDrops(rxDrops);
        stats.setReceiveRate(rxRate);
        stats.setReceivedBytes(rxTotal);
        stats.setReceivedBytesOffset(rxOffset);
        stats.setTransmitDrops(txDrops);
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
