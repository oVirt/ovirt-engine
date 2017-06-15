package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.compat.Guid;

public class UpdateGlusterVolumeSnapshotConfigParameters extends ActionParametersBase {
    private static final long serialVersionUID = 2015321730118872977L;

    private Guid clusterId;
    // Nullable, so not extending the class from GlusterVolumeParameters
    private Guid volumeId;
    private List<GlusterVolumeSnapshotConfig> configParams;

    public UpdateGlusterVolumeSnapshotConfigParameters() {
    }

    public UpdateGlusterVolumeSnapshotConfigParameters(Guid clusterId,
            Guid volumeId,
            List<GlusterVolumeSnapshotConfig> params) {
        this.clusterId = clusterId;
        this.volumeId = volumeId;
        this.configParams = params;
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid id) {
        this.clusterId = id;
    }

    public Guid getVolumeId() {
        return this.volumeId;
    }

    public void setVolumeId(Guid id) {
        this.volumeId = id;
    }

    public List<GlusterVolumeSnapshotConfig> getConfigParams() {
        return this.configParams;
    }

    public void setConfigParams(List<GlusterVolumeSnapshotConfig> params) {
        this.configParams = params;
    }
}
