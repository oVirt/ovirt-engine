package org.ovirt.engine.core.common.action;

import java.util.Date;

import org.ovirt.engine.core.compat.Guid;


public class ImagesActionsParametersBase extends StorageDomainParametersBase {
    private static final long serialVersionUID = -5791892465249711608L;

    private Guid imageId = Guid.Empty;
    private Guid destinationImageId = Guid.Empty;
    private String description;
    private Date oldLastModifiedValue;
    private Guid vmSnapshotId = Guid.Empty;
    private Guid imageGroupID = Guid.Empty;

    public ImagesActionsParametersBase() {
    }

    public ImagesActionsParametersBase(Guid imageId) {
        super(Guid.Empty);
        setEntityId(imageId);
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
