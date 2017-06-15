package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.LeaseStatus;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Image implements BusinessEntityWithStatus<Guid, ImageStatus> {

    private static final long serialVersionUID = -7058426105212449257L;

    private Guid id;

    private boolean active;

    private VolumeClassification volumeClassification;

    private Date creationDate;

    private Date lastModified;

    private Guid parentId;

    private Guid templateImageId;

    private Guid snapshotId;

    private Guid diskId;

    private ImageStatus status;

    @NotNull(message = "VALIDATION_VOLUME_TYPE_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType;

    private long size;

    @NotNull(message = "VALIDATION_VOLUME_FORMAT_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

    private QcowCompat qcowCompat = QcowCompat.Undefined;

    private LeaseStatus leaseStatus;

    private Integer generation;

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

    public QcowCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QcowCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }

    public VolumeClassification getVolumeClassification() {
        if (volumeClassification == null) {
            return active ? VolumeClassification.Volume : VolumeClassification.Snapshot;
        }
        return volumeClassification;
    }

    public void setVolumeClassification(VolumeClassification volumeClassification) {
        this.volumeClassification = volumeClassification;
    }


    public LeaseStatus getLeaseStatus() {
        return leaseStatus;
    }

    public void setLeaseStatus(LeaseStatus leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                active,
                lastModified,
                parentId,
                snapshotId,
                status,
                templateImageId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Image)) {
            return false;
        }
        Image other = (Image) obj;
        return Objects.equals(id, other.id)
                && active == other.active
                && Objects.equals(lastModified, other.lastModified)
                && Objects.equals(parentId, other.parentId)
                && Objects.equals(snapshotId, other.snapshotId)
                && status == other.status
                && Objects.equals(templateImageId, other.templateImageId);
    }
}
