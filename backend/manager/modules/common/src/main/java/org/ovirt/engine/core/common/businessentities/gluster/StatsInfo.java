package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

public class StatsInfo implements Serializable {

    private static final long serialVersionUID = 3609367118733238971L;

    private List<BlockStats> blockStats;
    private List<FopStats> fopStats;
    private int duration;
    private int totalRead;
    private int totalWrite;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTotalRead() {
        return totalRead;
    }

    public void setTotalRead(int totalRead) {
        this.totalRead = totalRead;
    }

    public int getTotalWrite() {
        return totalWrite;
    }

    public void setTotalWrite(int totalWrite) {
        this.totalWrite = totalWrite;
    }

    public ProfileStatsType getProfileStatsType() {
        return profileStatsType;
    }

    public void setProfileStatsType(ProfileStatsType profileStatsType) {
        this.profileStatsType = profileStatsType;
    }
}
