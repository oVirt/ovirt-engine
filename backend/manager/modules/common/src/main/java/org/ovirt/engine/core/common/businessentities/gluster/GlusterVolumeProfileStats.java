package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.ObjectUtils;

public class GlusterVolumeProfileStats implements Serializable, Nameable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<StatsInfo> profileStats;

    @Override
    public String getName() {
        return name;
    }

    public void setIdentity(String identity) {
        this.name = identity;
    }

    public List<StatsInfo> getProfileStats() {
        return profileStats;
    }

    public void setProfileStats(List<StatsInfo> profileStats) {
        this.profileStats = profileStats;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GlusterVolumeProfileStats)) {
            return false;
        }
        GlusterVolumeProfileStats profileDetails = (GlusterVolumeProfileStats) obj;
        if(!ObjectUtils.objectsEqual(getName(), profileDetails.getName())) {
            return false;
        }
        if(!ObjectUtils.objectsEqual(getProfileStats(), profileDetails.getProfileStats())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getProfileStats() == null) ? 0 : getProfileStats().hashCode());
        return result;
    }

}
