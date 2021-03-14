package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateSnapshotForVmParameters extends VmOperationParameterBase implements HostJobCommandParameters, Serializable {
    private static final long serialVersionUID = 847791941815264795L;

    @NotEmpty(groups = CreateEntity.class,
            message = "VALIDATION_DISK_IMAGE_DESCRIPTION_NOT_EMPTY")
    @ValidDescription(message = "VALIDATION_DISK_IMAGE_DESCRIPTION_INVALID", groups = CreateEntity.class)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, groups = CreateEntity.class,
            message = "VALIDATION_DISK_IMAGE_DESCRIPTION_MAX")
    private String description;

    private boolean needsLocking;

    /** Used to indicate the type of snapshot to take */
    private SnapshotType snapshotType = SnapshotType.REGULAR;

    /** Used to indicate whether the memory should be saved as part of this snapshot or not */
    private boolean saveMemory;

    private Guid createdSnapshotId;

    @JsonIgnore
    private Set<Guid> diskIdsToIgnoreInChecks;

    private Set<Guid> disks;

    private Map<Guid, DiskImage> diskImagesMap;

    private boolean liveSnapshotRequired;

    private boolean liveSnapshotSucceeded;

    private CreateSnapshotStage createSnapshotStage = CreateSnapshotStage.CREATE_VOLUME_STARTED;

    private Guid hostJobId;

    private Snapshot snapshot;

    private boolean legacyFlow;

    private List<DiskImage> cachedSelectedActiveDisks;

    private boolean memorySnapshotSupported;

    private boolean parentLiveMigrateDisk;

    private boolean shouldFreezeOrThaw;


    public CreateSnapshotForVmParameters() {
        needsLocking = true;
        saveMemory = true;
        diskIdsToIgnoreInChecks = Collections.emptySet();
        diskImagesMap = Collections.emptyMap();
        legacyFlow = false;
    }

    public CreateSnapshotForVmParameters(Guid vmId, String description) {
        super(vmId);
        this.description = description;
        needsLocking = true;
        saveMemory = true;
        diskIdsToIgnoreInChecks = Collections.emptySet();
        diskImagesMap = Collections.emptyMap();
        legacyFlow = false;
    }

    public CreateSnapshotForVmParameters(Guid vmId, String description, boolean saveMemory) {
        this(vmId, description);
        this.saveMemory = saveMemory;
    }

    public CreateSnapshotForVmParameters(Guid vmId, String description, boolean saveMemory, Set<Guid> diskIds) {
        this(vmId, description, saveMemory);
        this.disks = diskIds;
    }

    public String getDescription() {
        return description;
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    /**
     * This method is for internal use only, disregard in API.
     */
    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public boolean isSaveMemory() {
        return saveMemory;
    }

    public void setSaveMemory(boolean saveMemory) {
        this.saveMemory = saveMemory;
    }

    public boolean isNeedsLocking() {
        return needsLocking;
    }

    public void setNeedsLocking(boolean needsLocking) {
        this.needsLocking = needsLocking;
    }

    public Set<Guid> getDiskIdsToIgnoreInChecks() {
        return diskIdsToIgnoreInChecks;
    }

    public void setDiskIdsToIgnoreInChecks(Set<Guid> diskIdsToIgnoreInChecks) {
        this.diskIdsToIgnoreInChecks = diskIdsToIgnoreInChecks;
    }

    public Set<Guid> getDiskIds() {
        return disks;
    }

    public void setDiskIds(Set<Guid> diskIds) {
        this.disks = diskIds;
    }

    public Guid getCreatedSnapshotId() {
        return createdSnapshotId;
    }

    public void setCreatedSnapshotId(Guid createdSnapshotId) {
        this.createdSnapshotId = createdSnapshotId;
    }

    public Map<Guid, DiskImage> getDiskImagesMap() {
        return diskImagesMap;
    }

    public void setDiskImagesMap(Map<Guid, DiskImage> diskImagesMap) {
        this.diskImagesMap = diskImagesMap;
    }

    public CreateSnapshotStage getCreateSnapshotStage() {
        return createSnapshotStage;
    }

    public void setCreateSnapshotStage(CreateSnapshotStage createSnapshotStage) {
        this.createSnapshotStage = createSnapshotStage;
    }

    public boolean isLiveSnapshotRequired() {
        return liveSnapshotRequired;
    }

    public void setLiveSnapshotRequired(boolean liveSnapshotRequired) {
        this.liveSnapshotRequired = liveSnapshotRequired;
    }

    public boolean isLiveSnapshotSucceeded() {
        return liveSnapshotSucceeded;
    }

    public void setLiveSnapshotSucceeded(boolean liveSnapshotSucceeded) {
        this.liveSnapshotSucceeded = liveSnapshotSucceeded;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public Guid getHostJobId() {
        return hostJobId;
    }

    public void setHostJobId(Guid hostJobId) {
        this.hostJobId = hostJobId;
    }

    public void setLegacyFlow(boolean legacyFlow) {
        this.legacyFlow = legacyFlow;
    }

    public boolean isLegacyFlow() {
        return legacyFlow;
    }

    public void setCachedSelectedActiveDisks(List<DiskImage> cachedSelectedActiveDisks) {
        this.cachedSelectedActiveDisks = cachedSelectedActiveDisks;
    }

    public List<DiskImage> getCachedSelectedActiveDisks() {
        return cachedSelectedActiveDisks;
    }

    public void setMemorySnapshotSupported(boolean memorySnapshotSupported) {
        this.memorySnapshotSupported = memorySnapshotSupported;
    }

    public boolean isMemorySnapshotSupported() {
        return memorySnapshotSupported;
    }

    public void setParentLiveMigrateDisk(boolean parentLiveMigrateDisk) {
        this.parentLiveMigrateDisk = parentLiveMigrateDisk;
    }

    public boolean isParentLiveMigrateDisk() {
        return parentLiveMigrateDisk;
    }

    public void setShouldFreezeOrThaw(boolean shouldFreezeOrThaw) {
        this.shouldFreezeOrThaw = shouldFreezeOrThaw;
    }

    public boolean getShouldFreezeOrThaw() {
        return shouldFreezeOrThaw;
    }

    public enum CreateSnapshotStage {
        CREATE_VOLUME_STARTED,
        CREATE_VOLUME_FINISHED,
        CREATE_SNAPSHOT_STARTED,
        CREATE_SNAPSHOT_COMPLETED,
    }
}
