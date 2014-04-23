package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class BrickProfileDetails implements Serializable {

    private static final long serialVersionUID = 3609367118733238971L;

    private Guid brickId;
    private String brickName;
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

    public String getBrickName() {
        return brickName;
    }

    public void setBrickName(String brickName) {
        this.brickName = brickName;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BrickProfileDetails)) {
            return false;
        }
        BrickProfileDetails brickDetails = (BrickProfileDetails) obj;
        if(! (ObjectUtils.objectsEqual(getBrickId(), brickDetails.getBrickId()) &&
                ObjectUtils.objectsEqual(getBrickName(), brickDetails.getBrickName()))) {
            return false;
        }
        if(! (ObjectUtils.objectsEqual(getStatsInfo(), brickDetails.getStatsInfo()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBrickId() == null) ? 0 : getBrickId().hashCode());
        result = prime * result + ((getBrickName() == null) ? 0 : getBrickName().hashCode());
        result = prime * result + ((getStatsInfo() == null) ? 0 : getStatsInfo().hashCode());
        return result;
    }
}
