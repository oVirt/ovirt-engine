package org.ovirt.engine.core.common.action;

import org.hibernate.validator.constraints.NotEmpty;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllSnapshotsFromVmParameters extends VmOperationParameterBase implements java.io.Serializable {

    private static final long serialVersionUID = 3456740034406494984L;

    @NotEmpty(groups = { CreateEntity.class },
            message = "VALIDATION.DISK_IMAGE.DESCRIPTION.NOT_EMPTY")
    @ValidDescription(message = "VALIDATION.DISK_IMAGE.DESCRIPTION.NOT_ASCII", groups = { CreateEntity.class})
    private String _description;

    /**
     * Used to indicate the type of snapshot to take.
     */
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

    /**
     * This method is for internal use only, disregard in API.
     * @param snapshotType
     */
    public void setSnapshotType(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
    }
}
