package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateSnapshotDiskParameters extends VmOperationParameterBase implements Serializable {

    private static final long serialVersionUID = -1341054636243287904L;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, groups = CreateEntity.class,
            message = "VALIDATION_DISK_IMAGE_DESCRIPTION_MAX")
    private String description;
    private Set<Guid> disks;
    private Guid newActiveSnapshotId;
    private Map<Guid, DiskImage> diskImagesMap;
    private boolean isLiveSnapshot;

    @JsonIgnore
    private Set<Guid> diskIdsToIgnoreInChecks;

    /** Used to indicate the type of snapshot to take */
    private Snapshot.SnapshotType snapshotType = Snapshot.SnapshotType.REGULAR;


    public String getDescription() {
        return description;
    }

    public Set<Guid> getDiskIds() {
        return disks;
    }

    public void setDiskIds(Set<Guid> disks) {
        this.disks = disks;
    }

    public Guid getNewActiveSnapshotId() {
        return newActiveSnapshotId;
    }

    public void setNewActiveSnapshotId(Guid newActiveSnapshotId) {
        this.newActiveSnapshotId = newActiveSnapshotId;
    }

    public Map<Guid, DiskImage> getDiskImagesMap() {
        return diskImagesMap;
    }

    public void setDiskImagesMap(Map<Guid, DiskImage> diskImagesMap) {
        this.diskImagesMap = diskImagesMap;
    }

    public Set<Guid> getDiskIdsToIgnoreInChecks() {
        return diskIdsToIgnoreInChecks;
    }

    public void setDiskIdsToIgnoreInChecks(Set<Guid> diskIdsToIgnoreInChecks) {
        this.diskIdsToIgnoreInChecks = diskIdsToIgnoreInChecks;
    }

    public Snapshot.SnapshotType getSnapshotType() {
        return snapshotType;
    }

    /**
     * This method is for internal use only, disregard in API.
     */
    public void setSnapshotType(Snapshot.SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public boolean isLiveSnapshot() {
        return isLiveSnapshot;
    }

    public void setLiveSnapshot(boolean isLiveSnapshot) {
        this.isLiveSnapshot = isLiveSnapshot;
    }
}
