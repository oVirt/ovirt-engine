package org.ovirt.engine.core.vdsbroker;

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
        existingStats.setReceiveDropRate(reportedStats.getReceiveDropRate());
        existingStats.setTransmitDropRate(reportedStats.getTransmitDropRate());

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

    private EffectiveStats computeEffectiveStats(Long reported, Long previous, Long offset) {
        EffectiveStats stats = new EffectiveStats();

        if (reported == null) {
            // the value wasn't reported - clear it, keep the previous offset, and rate can't be computed
            stats.current = null;
            stats.offset = offset;
            stats.rate = null;
        } else if (offset == null) {
            // statistic reported for the first time - set to zero, set offset accordingly, and rate can't be computed
            stats.current = 0L;
            stats.offset = -reported;
            stats.rate = null;
        } else {
            stats.offset = offset;
            stats.current = offset + reported;
            if (previous == null) {
                // for some reason there's no previous sample - the rate can't be computed
                stats.rate = null;
            } else {
                if (stats.current < previous) {
                    // if value wrapped around, it means counter had reset - compensate in offset, and update value
                    stats.offset = previous;
                    stats.current = previous + reported;
                }

                // current and previous sampled values are up-to-date - try to compute rate
                stats.rate = computeRatePercentage(stats.current - previous);
            }
        }

        return stats;
    }

    private static double truncatePercentage(double value) {
        return Math.min(100, value);
    }

    private Double computeRatePercentage(long byteDiff) {
        if (currentTime == null || previousTime == null || currentTime <= previousTime || speed == null
                || speed.equals(0)) {
            return null;
        }

        long megabitDiff = BITS_IN_BYTE * byteDiff / BITS_IN_MEGABIT;
        double timeDiffInSeconds = currentTime - previousTime;
        double rateInMbps = megabitDiff / timeDiffInSeconds;
        return truncatePercentage(100 * rateInMbps / speed);
    }

    private static class EffectiveStats {
        private Long current;
        private Long offset;
        private Double rate;
    }

}
