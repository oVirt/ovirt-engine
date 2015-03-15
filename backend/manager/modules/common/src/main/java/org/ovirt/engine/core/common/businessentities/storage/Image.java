package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Image implements BusinessEntityWithStatus<Guid, ImageStatus> {

    private static final long serialVersionUID = -7058426105212449257L;

    private Guid id;

    private boolean active;

    private Date creationDate;

    private Date lastModified;

    private Guid parentId;

    private Guid templateImageId;

    private Guid snapshotId;

    private Guid diskId;

    private ImageStatus status;

    @NotNull(message = "VALIDATION.VOLUME_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType;

    private long size;

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

    public Image() {
        id = Guid.Empty;
        creationDate = new Date();
        lastModified = new Date(0);
        parentId = Guid.Empty;
        templateImageId = Guid.Empty;
        status = ImageStatus.Unassigned;
        volumeType = VolumeType.Sparse;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Guid getParentId() {
        return parentId;
    }

    public void setParentId(Guid parentId) {
        this.parentId = parentId;
    }

    public Guid getTemplateImageId() {
        return templateImageId;
    }

    public void setTemplateImageId(Guid templateImageId) {
        this.templateImageId = templateImageId;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    @Override
    public ImageStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ImageStatus status) {
        this.status = status;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((snapshotId == null) ? 0 : snapshotId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((templateImageId == null) ? 0 : templateImageId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Image)) {
            return false;
        }
        Image other = (Image) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && active == other.active
                && ObjectUtils.objectsEqual(lastModified, other.lastModified)
                && ObjectUtils.objectsEqual(parentId, other.parentId)
                && ObjectUtils.objectsEqual(snapshotId, other.snapshotId)
                && status == other.status
                && ObjectUtils.objectsEqual(templateImageId, other.templateImageId));
    }
}
