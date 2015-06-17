package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllCinderSnapshotsParameters extends VmOperationParameterBase implements Serializable {

    private boolean parentHasTasks;
    private Guid imageId;
    private Snapshot snapshot;
    private Guid removedSnapshotId;
    private boolean privateForceDelete;
    private List<CinderDisk> cinderDisks;

    public RestoreAllCinderSnapshotsParameters() {
    }

    public RestoreAllCinderSnapshotsParameters(Guid vmId, List<CinderDisk> cinderDisks) {
        super(vmId);
        this.cinderDisks = cinderDisks;
        setForceDelete(false);
    }

    public List<CinderDisk> getCinderDisks() {
        return cinderDisks;
    }

    public void setCinderDisks(List<CinderDisk> cinderDisks) {
        this.cinderDisks = cinderDisks;
    }

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public boolean isParentHasTasks() {
        return parentHasTasks;
    }

    public void setParentHasTasks(boolean parentHasTasks) {
        this.parentHasTasks = parentHasTasks;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Guid getRemovedSnapshotId() {
        return removedSnapshotId;
    }

    public void setRemovedSnapshotId(Guid removedSnapshotId) {
        this.removedSnapshotId = removedSnapshotId;
    }
}
