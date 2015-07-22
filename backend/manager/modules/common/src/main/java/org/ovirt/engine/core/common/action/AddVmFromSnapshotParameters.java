package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for AddVmFromSnapshot command
 */
public class AddVmFromSnapshotParameters extends AddVmParameters implements Serializable {
    private static final long serialVersionUID = -3400982291165788716L;

    //Unique Identifier of Source Snapshot
    @NotNull(message="VALIDATION_SOURCE_SNAPSHOT_ID_NOT_NULL")
    private Guid sourceSnapshotId;

    public AddVmFromSnapshotParameters() {
    }

    public AddVmFromSnapshotParameters(VmStatic vmStatic, Guid sourceSnapshotId) {
        super(vmStatic);
        setVmId(Guid.Empty);
        this.sourceSnapshotId = sourceSnapshotId;
    }

    public Guid getSourceSnapshotId() {
        return sourceSnapshotId;
    }

    public void setSourceSnapshotId(Guid sourceSnapshotId) {
        this.sourceSnapshotId = sourceSnapshotId;
    }
}
