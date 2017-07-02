package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotEntity implements Queryable, BusinessEntityWithStatus<Guid, GlusterSnapshotStatus> {
    private static final long serialVersionUID = 2355384696827317288L;
    private Guid snapshotId;
    private Guid clusterId;
    private Guid volumeId;
    private String snapshotName;
    private Date createdAt;
    private String description;
    private GlusterSnapshotStatus status;

    @Override
    public Guid getId() {
        return snapshotId;
    }

    @Override
    public void setId(Guid id) {
        snapshotId = id;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid cid) {
        this.clusterId = cid;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public GlusterSnapshotStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(GlusterSnapshotStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                snapshotId,
                clusterId,
                volumeId,
                snapshotName,
                description,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeSnapshotEntity)) {
            return false;
        }

        GlusterVolumeSnapshotEntity other = (GlusterVolumeSnapshotEntity) obj;
        return Objects.equals(snapshotId, other.snapshotId)
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(volumeId, other.volumeId)
                && Objects.equals(snapshotName, other.snapshotName)
                && Objects.equals(description, other.description)
                && status == other.status;
    }

    @Override
    public Object getQueryableId() {
        return this.snapshotId;
    }
}
