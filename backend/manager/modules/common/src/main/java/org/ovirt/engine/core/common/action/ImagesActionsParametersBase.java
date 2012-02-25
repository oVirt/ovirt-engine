package org.ovirt.engine.core.common.action;

import java.util.Date;

import org.ovirt.engine.core.compat.Guid;


public class ImagesActionsParametersBase extends StorageDomainParametersBase {
    private static final long serialVersionUID = -5791892465249711608L;

    private Guid imageId = new Guid();
    private Guid destinationImageId = new Guid();
    private String description;
    private Date oldLastModifiedValue;
    private Guid vmSnapshotId = new Guid();
    private Guid imageGroupID = new Guid();

    public ImagesActionsParametersBase() {
    }

    public ImagesActionsParametersBase(Guid imageId) {
        super(Guid.Empty);
        this.imageId = imageId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public Guid getDestinationImageId() {
        return destinationImageId;
    }

    public void setDestinationImageId(Guid value) {
        destinationImageId = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    public void setOldLastModifiedValue(Date oldLastModifiedValue) {
        this.oldLastModifiedValue = oldLastModifiedValue;
    }

    public Date getOldLastModifiedValue() {
        return oldLastModifiedValue;
    }

    public Guid getVmSnapshotId() {
        return vmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        vmSnapshotId = value;
    }

    public Guid getImageGroupID() {
        return imageGroupID;
    }

    public void setImageGroupID(Guid value) {
        imageGroupID = value;
    }
}
