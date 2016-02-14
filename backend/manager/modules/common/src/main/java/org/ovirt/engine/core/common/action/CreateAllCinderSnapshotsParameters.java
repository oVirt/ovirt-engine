package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllCinderSnapshotsParameters extends VdcActionParametersBase implements Serializable {

    private List<CinderDisk> cinderDisks;
    private String description;
    private Snapshot.SnapshotType snapshotType;
    private Guid newActiveSnapshotId;

    public CreateAllCinderSnapshotsParameters() {
    }

    public CreateAllCinderSnapshotsParameters(List<CinderDisk> cinderDisks, String description,
                                              Snapshot.SnapshotType snapshotType, Guid newActiveSnapshotId) {
        this.cinderDisks = cinderDisks;
        this.description = description;
        this.snapshotType = snapshotType;
        this.newActiveSnapshotId = newActiveSnapshotId;
    }

    public List<CinderDisk> getCinderDisks() {
        return cinderDisks;
    }

    public void setCinderDisks(List<CinderDisk> cinderDisks) {
        this.cinderDisks = cinderDisks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Snapshot.SnapshotType getSnapshotType() {
        return snapshotType;
    }


    public void setSnapshotType(Snapshot.SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public Guid getNewActiveSnapshotId() {
        return newActiveSnapshotId;
    }

    public void setNewActiveSnapshotId(Guid newActiveSnapshotId) {
        this.newActiveSnapshotId = newActiveSnapshotId;
    }
}
