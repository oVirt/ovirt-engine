package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllSnapshotsParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -8756081739745132849L;

    private List<DiskImage> images;
    private SnapshotActionEnum snapshotAction;

    // We need to keep the snapshot id in the parameters for the tasks,
    // so we can unlock it when the restore finishes.
    private Guid snapshotId;

    public RestoreAllSnapshotsParameters(Guid vmId, SnapshotActionEnum snapshotAction) {
        super(vmId);
        this.snapshotAction = snapshotAction;
    }

    public List<DiskImage> getImages() {
        return images;
    }

    public void setImages(List<DiskImage> images) {
        this.images = images;
    }

    public RestoreAllSnapshotsParameters() {
    }

    public SnapshotActionEnum getSnapshotAction() {
        return snapshotAction;
    }

    public void setSnapshotAction(SnapshotActionEnum snapshotAction) {
        this.snapshotAction = snapshotAction;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }
}
