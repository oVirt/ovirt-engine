package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.utils.ObjectUtils;

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
        if (!(obj instanceof GlusterSnapshotConfigInfo)) {
            return false;
        }
        GlusterSnapshotConfigInfo configInfo = (GlusterSnapshotConfigInfo) obj;
        if (!ObjectUtils.objectsEqual(getClusterConfigOptions(), configInfo.getClusterConfigOptions())) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(getVolumeConfigOptions(), configInfo.getVolumeConfigOptions())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getClusterConfigOptions() == null) ? 0 : getClusterConfigOptions().hashCode());
        result =
                prime * result + ((getVolumeConfigOptions() == null) ? 0 : getVolumeConfigOptions().hashCode());
        return result;
    }
}
