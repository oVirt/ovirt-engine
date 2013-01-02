package org.ovirt.engine.core.common.action;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllSnapshotsFromVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 847791941815264795L;

    @NotEmpty(groups = { CreateEntity.class },
            message = "VALIDATION.DISK_IMAGE.DESCRIPTION.NOT_EMPTY")
    @ValidDescription(message = "VALIDATION.DISK_IMAGE.DESCRIPTION.INVALID", groups = { CreateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE, groups = { CreateEntity.class},
            message = "VALIDATION_DISK_IMAGE_DESCRIPTION_MAX")
    private String _description;

    private boolean needsLocking = true;
    /** Used to store the vm status when the command start, will be used to check if the vm went down during the execution */
    private VMStatus initialVmStatus;

    /** Used to indicate the type of snapshot to take */
    private SnapshotType snapshotType;

    public CreateAllSnapshotsFromVmParameters() {
    }

    public CreateAllSnapshotsFromVmParameters(Guid vmId, String description) {
        super(vmId);
        _description = description;
    }

    public String getDescription() {
        return _description;
    }

    public SnapshotType getSnapshotType() {
        return snapshotType;
    }

    public void setInitialVmStatus(VMStatus vmStatus) {
        this.initialVmStatus = vmStatus;
    }

    public VMStatus getInitialVmStatus() {
        return initialVmStatus;
    }

    /**
     * This method is for internal use only, disregard in API.
     * @param snapshotType
     */
    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }

    public boolean isNeedsLocking() {
        return needsLocking;
    }

    public void setNeedsLocking(boolean needsLocking) {
        this.needsLocking = needsLocking;
    }
}
