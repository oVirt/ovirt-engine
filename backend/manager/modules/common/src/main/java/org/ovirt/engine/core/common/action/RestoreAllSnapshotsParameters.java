package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllSnapshotsParameters extends TryBackToAllSnapshotsOfVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = -8756081739745132849L;

    private List<DiskImage> images;

    public RestoreAllSnapshotsParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId, dstSnapshotId);
    }

    public RestoreAllSnapshotsParameters(Guid vmId, Guid dstSnapshotId, List<DiskImage> images) {
        this(vmId, dstSnapshotId);
        this.images = images;
    }

    public List<DiskImage> getImages() {
        return images;
    }

    public void setImages(List<DiskImage> images) {
        this.images = images;
    }

    public RestoreAllSnapshotsParameters() {
    }
}
