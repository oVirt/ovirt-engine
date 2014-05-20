package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.utils.Pair;

public class StatsInfo implements Serializable {

    private static final long serialVersionUID = 3609367118733238971L;

    private List<BlockStats> blockStats;
    private List<FopStats> fopStats;
    private int duration;
    private Pair<Integer, String> durationFormatted;
    private long totalRead;
    private long totalWrite;
    private ProfileStatsType profileStatsType;

    public List<BlockStats> getBlockStats() {
        return blockStats;
    }

    public void setBlockStats(List<BlockStats> blockStats) {
        this.blockStats = blockStats;
    }

    public List<FopStats> getFopStats() {
        return fopStats;
    }

    public void setFopStats(List<FopStats> fopStats) {
        this.fopStats = fopStats;
    }

    public long getTotalRead() {
        return totalRead;
    }

    public void setTotalRead(long totalRead) {
        this.totalRead = totalRead;
    }

    public long getTotalWrite() {
        return totalWrite;
    }

    public void setTotalWrite(long totalWrite) {
        this.totalWrite = totalWrite;
    }

    public ProfileStatsType getProfileStatsType() {
        return profileStatsType;
    }

    public void setProfileStatsType(ProfileStatsType profileStatsType) {
        this.profileStatsType = profileStatsType;
    }

    public Pair<Integer, String> getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(Pair<Integer, String> duration) {
        this.durationFormatted = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
