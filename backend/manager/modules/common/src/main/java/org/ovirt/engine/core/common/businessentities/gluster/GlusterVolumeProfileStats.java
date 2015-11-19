package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Nameable;

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
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof GlusterVolumeProfileStats)) {
            return false;
        }
        GlusterVolumeProfileStats other = (GlusterVolumeProfileStats) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(profileStats, other.profileStats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                profileStats
        );
    }

}
