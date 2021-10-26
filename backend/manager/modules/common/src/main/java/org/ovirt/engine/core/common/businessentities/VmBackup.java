package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class VmBackup implements Queryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = 1155292523061178984L;

    private Guid id;

    private Guid vmId;

    private Guid hostId;

    private Guid fromCheckpointId;

    private Guid toCheckpointId;

    private VmBackupPhase phase;

    private Date creationDate;

    private Date modificationDate;

    private String description;

    private List<DiskImage> disks;

    private boolean liveBackup;

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public Guid getFromCheckpointId() {
        return fromCheckpointId;
    }

    public void setFromCheckpointId(Guid fromCheckpointId) {
        this.fromCheckpointId = fromCheckpointId;
    }

    public Guid getToCheckpointId() {
        return toCheckpointId;
    }

    public void setToCheckpointId(Guid toCheckpointId) {
        this.toCheckpointId = toCheckpointId;
    }

    public VmBackupPhase getPhase() {
        return phase;
    }

    public void setPhase(VmBackupPhase phase) {
        this.phase = phase;
    }

    public List<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskImage> disks) {
        this.disks = disks;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIncremental() {
        return fromCheckpointId != null;
    }

    public boolean isLiveBackup() {
        return liveBackup;
    }

    public void setLiveBackup(boolean liveBackup) {
        this.liveBackup = liveBackup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                vmId,
                hostId,
                fromCheckpointId,
                toCheckpointId,
                phase,
                disks,
                creationDate,
                modificationDate,
                description,
                liveBackup
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmBackup)) {
            return false;
        }
        VmBackup other = (VmBackup) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(vmId, other.vmId)
                && Objects.equals(hostId, other.hostId)
                && Objects.equals(fromCheckpointId, other.fromCheckpointId)
                && Objects.equals(toCheckpointId, other.toCheckpointId)
                && Objects.equals(phase, other.phase)
                && Objects.equals(disks, other.disks)
                && Objects.equals(creationDate, other.creationDate)
                && Objects.equals(modificationDate, other.modificationDate)
                && Objects.equals(description, other.description)
                && Objects.equals(liveBackup, other.liveBackup);
    }

    @Override
    public Object getQueryableId() {
        return id;
    }
}
