package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeActivateSnapshotVDSParameters extends GlusterVolumeSnapshotActionVDSParameters {
    protected boolean force;

    public GlusterVolumeActivateSnapshotVDSParameters() {
    }

    public GlusterVolumeActivateSnapshotVDSParameters(Guid serverId,
            String volumeName,
            String snapshotName,
            boolean force) {
        super(serverId, volumeName, snapshotName);
        this.force = force;
    }

    public boolean getForce() {
        return this.force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("force", getForce());
    }
}
