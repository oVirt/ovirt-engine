package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class GlusterSnapshotConfigInfo implements Serializable {
    private static final long serialVersionUID = -768822766895441288L;

    private Map<String, String> clusterConfigOptions;
    private Map<String, Map<String, String>> volumeConfigOptions;

    public Map<String, String> getClusterConfigOptions() {
        return this.clusterConfigOptions;
    }

    public void setClusterConfigOptions(Map<String, String> options) {
        this.clusterConfigOptions = options;
    }

    public Map<String, Map<String, String>> getVolumeConfigOptions() {
        return this.volumeConfigOptions;
    }

    public void setVolumeConfigOptions(Map<String, Map<String, String>> options) {
        this.volumeConfigOptions = options;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterSnapshotConfigInfo)) {
            return false;
        }
        GlusterSnapshotConfigInfo other = (GlusterSnapshotConfigInfo) obj;
        return Objects.equals(clusterConfigOptions, other.clusterConfigOptions)
                && Objects.equals(volumeConfigOptions, other.volumeConfigOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clusterConfigOptions,
                volumeConfigOptions
        );
    }
}
