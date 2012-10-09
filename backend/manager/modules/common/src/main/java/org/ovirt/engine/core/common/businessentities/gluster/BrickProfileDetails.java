package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class BrickProfileDetails implements Serializable {

    private static final long serialVersionUID = 3609367118733238971L;

    private Guid brickId;
    private List<StatsInfo> statsInfo;

    public BrickProfileDetails() {
    }

    public Guid getBrickId() {
        return brickId;
    }

    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    public List<StatsInfo> getStatsInfo() {
        return statsInfo;
    }

    public void setStatsInfo(List<StatsInfo> statsInfo) {
        this.statsInfo = statsInfo;
    }
}
