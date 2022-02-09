package org.ovirt.engine.core.vdsbroker;

import java.math.BigInteger;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;

public class NetworkStatisticsBuilder {

    private static final int BITS_IN_BYTE = 8;
    private static final int BITS_IN_MEGABIT = 1000000;

    private Integer speed;
    private Double currentTime;
    private Double previousTime;

    public NetworkStatisticsBuilder() {
    }

    /**
     * Updates an existing NetworkInterface entity with recent statistics stored in a new NetworkInterface entity.
     *
     * @param existingIface
     *            the existing NetworkInterface entity, whose Statistics and Speed members are to be modified.
     * @param reportedIface
     *            the NetworkInterface entity storing recently-reported values, which will not be modified.
     */
    public void updateExistingInterfaceStatistics(NetworkInterface<?> existingIface, NetworkInterface<?> reportedIface) {
        NetworkStatistics existingStats = existingIface.getStatistics();
        NetworkStatistics reportedStats = reportedIface.getStatistics();

        speed = reportedIface.getSpeed();
        currentTime = reportedStats.getSampleTime();
        previousTime = existingStats.getSampleTime();

        existingIface.setSpeed(speed);
        existingStats.setReceiveDrops(reportedStats.getReceiveDrops());
        existingStats.setTransmitDrops(reportedStats.getTransmitDrops());

        EffectiveStats rxResult =
                computeEffectiveStats(reportedStats.getReceivedBytes(),
                        existingStats.getReceivedBytes(),
                        existingStats.getReceivedBytesOffset());
        EffectiveStats txResult =
                computeEffectiveStats(reportedStats.getTransmittedBytes(),
                        existingStats.getTransmittedBytes(),
                        existingStats.getTransmittedBytesOffset());

        existingStats.setReceivedBytes(rxResult.current);
        existingStats.setReceivedBytesOffset(rxResult.offset);
        existingStats.setReceiveRate(rxResult.rate);
        existingStats.setTransmittedBytes(txResult.current);
        existingStats.setTransmittedBytesOffset(txResult.offset);
        existingStats.setTransmitRate(txResult.rate);
        existingStats.setSampleTime(currentTime);
    }

    private EffectiveStats computeEffectiveStats(BigInteger reported, BigInteger previous, BigInteger previousOffset) {
        EffectiveStats stats = new EffectiveStats();

        if (reported == null) {
            // the value wasn't reported - clear it, keep the previous offset, and rate can't be computed
            stats.current = null;
            stats.offset = previousOffset;
            stats.rate = null;
        } else if (previousOffset == null) {
            // statistic reported for the first time - set to zero, set offset accordingly, and rate can't be computed
            stats.current = BigInteger.ZERO; //$NON-NLS-1$
            stats.offset = reported.negate();
            stats.rate = null;
        } else {
            stats.offset = previousOffset;
            stats.current = previousOffset.add(reported);
            if (previous == null) {
                // for some reason there's no previous sample - the rate can't be computed
                stats.rate = null;
            } else {
                if (stats.current.compareTo(previous) < 0) {
                    // if value wrapped around, it means counter had reset - compensate in offset, and update value
                    stats.offset = previous;
                    stats.current = previous.add(reported);
                }

                // current and previous sampled values are up-to-date - try to compute rate
                stats.rate = computeRatePercentage(stats.current.subtract(previous));
            }
        }

        return stats;
    }

    private static double truncatePercentage(double value) {
        return Math.min(100, value);
    }

    private Double computeRatePercentage(BigInteger byteDiff) {
        if (currentTime == null || previousTime == null || currentTime <= previousTime || speed == null
                || speed.equals(0)) {
            return null;
        }

        double megabitDiff = BITS_IN_BYTE * byteDiff.doubleValue() / BITS_IN_MEGABIT;
        double timeDiffInSeconds = currentTime - previousTime;
        double rateInMbps = megabitDiff / timeDiffInSeconds;
        return truncatePercentage(100 * rateInMbps / speed);
    }

    private static class EffectiveStats {
        private BigInteger current;
        private BigInteger offset;
        private Double rate;
    }

}
