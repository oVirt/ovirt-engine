package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("configParam", configParam);
    }
}
