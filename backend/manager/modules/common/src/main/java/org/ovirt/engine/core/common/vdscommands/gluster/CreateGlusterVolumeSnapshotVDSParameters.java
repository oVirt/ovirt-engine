package org.ovirt.engine.core.common.vdscommands.gluster;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateGlusterVolumeSnapshotVDSParameters extends GlusterVolumeVDSParameters {
    protected String snapshotName;
    protected String description;
    protected boolean force;

    public CreateGlusterVolumeSnapshotVDSParameters() {
    }

    public CreateGlusterVolumeSnapshotVDSParameters(Guid serverId,
            String volumeName,
            String snapshotName,
            String description,
            boolean force) {
        super(serverId, volumeName);
        this.snapshotName = snapshotName;
        this.description = description;
        this.force = force;
    }

    public String getSnapshotName() {
        return this.snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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
                .append("snapshotName", getSnapshotName())
                .append("description", getDescription())
                .append("force", getForce());
    }
}
