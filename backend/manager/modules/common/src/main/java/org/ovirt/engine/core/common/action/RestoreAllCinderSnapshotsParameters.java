package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RestoreAllCinderSnapshotsParameters extends VmOperationParameterBase implements Serializable {

    private Guid imageId;
    private Snapshot snapshot;
    private Guid removedSnapshotId;
    private boolean privateForceDelete;
    private boolean parentHasTasks;
    private List<CinderDisk> cinderDisksToRestore;
    private List<CinderDisk> cinderDisksToRemove;
    private List<CinderDisk> cinderVolumesToRemove;

    public RestoreAllCinderSnapshotsParameters() {
    }

    public RestoreAllCinderSnapshotsParameters(Guid vmId,
            List<CinderDisk> cinderDisksToRestore,
            List<CinderDisk> cinderDisksToRemove,
            List<CinderDisk> cinderVolumesToRemove) {
        super(vmId);
        this.cinderDisksToRestore = cinderDisksToRestore;
        this.cinderDisksToRemove = cinderDisksToRemove;
        this.cinderVolumesToRemove = cinderVolumesToRemove;
        setForceDelete(false);
    }

    public List<CinderDisk> getCinderDisksToRestore() {
        return cinderDisksToRestore;
    }

    public void setCinderDisksToRestore(List<CinderDisk> cinderDisksToRestore) {
        this.cinderDisksToRestore = cinderDisksToRestore;
    }

    public List<CinderDisk> getCinderDisksToRemove() {
        return cinderDisksToRemove;
    }

    public void setCinderDisksToRemove(List<CinderDisk> cinderDisksToRemove) {
        this.cinderDisksToRemove = cinderDisksToRemove;
    }

    public List<CinderDisk> getCinderVolumesToRemove() {
        return cinderVolumesToRemove;
    }

    public void setCinderVolumesToRemove(List<CinderDisk> cinderVolumesToRemove) {
        this.cinderVolumesToRemove = cinderVolumesToRemove;
    }

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
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

    public boolean isParentHasTasks() {
        return parentHasTasks;
    }

    public void setParentHasTasks(boolean parentHasTasks) {
        this.parentHasTasks = parentHasTasks;
    }
}
