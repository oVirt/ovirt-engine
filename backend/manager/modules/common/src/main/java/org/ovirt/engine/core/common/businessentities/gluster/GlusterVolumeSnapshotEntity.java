package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Date;

import javax.inject.Named;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

@Named
public class GlusterVolumeSnapshotEntity extends IVdcQueryable implements BusinessEntityWithStatus<Guid, GlusterSnapshotStatus> {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((snapshotId == null) ? 0 : snapshotId.hashCode());
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
        result = prime * result + ((volumeId == null) ? 0 : volumeId.hashCode());
        result = prime * result + ((snapshotName == null) ? 0 : snapshotName.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlusterVolumeSnapshotEntity)) {
            return false;
        }

        GlusterVolumeSnapshotEntity snapshot = (GlusterVolumeSnapshotEntity) obj;

        if (!(ObjectUtils.objectsEqual(snapshotId, snapshot.getSnapshotId()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(clusterId, snapshot.getClusterId()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(volumeId, snapshot.getVolumeId()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(snapshotName, snapshot.getSnapshotName()))) {
            return false;
        }

        if (!(ObjectUtils.objectsEqual(description, snapshot.getDescription()))) {
            return false;
        }

        if (status != snapshot.getStatus()) {
            return false;
        }

        return true;
    }

    @Override
    public Object getQueryableId() {
        return this.snapshotId;
    }
}
