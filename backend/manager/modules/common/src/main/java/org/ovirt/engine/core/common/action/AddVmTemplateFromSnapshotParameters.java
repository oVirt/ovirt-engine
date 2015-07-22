package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateFromSnapshotParameters extends AddVmTemplateParameters implements Serializable {
    private static final long serialVersionUID = -486319959050997796L;

    //Unique Identifier of Source Snapshot
    @NotNull(message="VALIDATION_SOURCE_SNAPSHOT_ID_NOT_NULL")
    private Guid sourceSnapshotId;

    public AddVmTemplateFromSnapshotParameters() {
    }

    public AddVmTemplateFromSnapshotParameters(VmStatic vmStatic, String name, String description, Guid sourceSnapshotId) {
        super(vmStatic, name, description);
        this.sourceSnapshotId = sourceSnapshotId;
    }

    public Guid getSourceSnapshotId() {
        return sourceSnapshotId;
    }

    public void setSourceSnapshotId(Guid sourceSnapshotId) {
        this.sourceSnapshotId = sourceSnapshotId;
    }
}
