package org.ovirt.engine.core.common.businessentities;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Image implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -7058426105212449257L;

    private Guid id = Guid.Empty;

    private boolean active;

    private Date creationDate = new Date();

    private Date lastModified = new Date(0);

    private Guid parentId = Guid.Empty;

    private Guid templateImageId = Guid.Empty;

    private Guid snapshotId;

    private Guid diskId;

    private ImageStatus status = ImageStatus.Unassigned;

    @NotNull(message = "VALIDATION.VOLUME_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType = VolumeType.Sparse;

    private long size = 0L;

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

    /**
     * The quota id the image consumes from.
     */
    private Guid quotaId;

    public Image() {
    }

    public Image(Guid id,
            boolean active,
            boolean boot,
            Date creationDate,
            Date lastModified,
            Guid parentId,
            Guid templateImageId,
            Guid snapshotId,
            Guid diskId,
            ImageStatus status,
            VolumeType volumeType,
            long size,
            VolumeFormat volumeFormat,
            Guid quotaId) {
        this.id = id;
        this.active = active;
        this.creationDate = creationDate;
        this.lastModified = lastModified;
        this.parentId = parentId;
        this.templateImageId = templateImageId;
        this.snapshotId = snapshotId;
        this.diskId = diskId;
        this.status = status;
        this.volumeType = volumeType;
        this.size = size;
        this.volumeFormat = volumeFormat;
        this.quotaId = quotaId;
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

    public ImageStatus getStatus() {
        return status;
    }

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

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
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
