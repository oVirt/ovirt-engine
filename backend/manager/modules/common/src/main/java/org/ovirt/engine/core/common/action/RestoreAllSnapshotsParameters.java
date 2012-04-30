package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import java.util.List;

public class RestoreAllSnapshotsParameters extends TryBackToAllSnapshotsOfVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = -8756081739745132849L;

    public RestoreAllSnapshotsParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId, dstSnapshotId);
    }

    private List<DiskImage> privateImagesList;

    public List<DiskImage> getImagesList() {
        return privateImagesList;
    }

    public void setImagesList(List<DiskImage> value) {
        privateImagesList = value;
    }

    public RestoreAllSnapshotsParameters() {
    }
}
