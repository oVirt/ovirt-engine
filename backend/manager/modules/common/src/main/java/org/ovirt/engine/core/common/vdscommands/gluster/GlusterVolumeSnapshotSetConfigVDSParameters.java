package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotSetConfigVDSParameters extends VdsIdVDSCommandParametersBase {
    GlusterVolumeSnapshotConfig configParam;

    public GlusterVolumeSnapshotSetConfigVDSParameters() {
    }

    public GlusterVolumeSnapshotSetConfigVDSParameters(Guid serverId, GlusterVolumeSnapshotConfig configParam) {
        super(serverId);
        this.configParam = configParam;
    }

    public GlusterVolumeSnapshotConfig getConfgParam() {
        return this.configParam;
    }

    @Override
    public String toString() {
        return String.format("%s, configParam=%s", super.toString(), configParam);
    }
}
