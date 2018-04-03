package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class CreateSnapshotForVmParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = 847791941815264795L;

    @NotEmpty(groups = { CreateEntity.class },
            message = "VALIDATION_DISK_IMAGE_DESCRIPTION_NOT_EMPTY")
    @ValidDescription(message = "VALIDATION_DISK_IMAGE_DESCRIPTION_INVALID", groups = { CreateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, groups = { CreateEntity.class },
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

    private Map<Guid, Guid> diskToImageIds;

    private boolean liveSnapshotRequired;

    private boolean liveSnapshotSucceeded;

    private CreateSnapshotStage createSnapshotStage = CreateSnapshotStage.CREATE_VOLUME;

    public CreateSnapshotForVmParameters() {
        needsLocking = true;
        saveMemory = true;
        diskIdsToIgnoreInChecks = Collections.emptySet();
        diskToImageIds = Collections.emptyMap();
    }

    public CreateSnapshotForVmParameters(Guid vmId, String description) {
        super(vmId);
        this.description = description;
        needsLocking = true;
        saveMemory = true;
        diskIdsToIgnoreInChecks = Collections.emptySet();
        diskToImageIds = Collections.emptyMap();
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

    public Map<Guid, Guid> getDiskToImageIds() {
        return diskToImageIds;
    }

    public void setDiskToImageIds(Map<Guid, Guid> diskToImageIds) {
        this.diskToImageIds = diskToImageIds;
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

    public enum CreateSnapshotStage {
        CREATE_VOLUME,
        CREATE_SNAPSHOT_STARTED,
        CREATE_SNAPSHOT_COMPLETED,
    }
}
