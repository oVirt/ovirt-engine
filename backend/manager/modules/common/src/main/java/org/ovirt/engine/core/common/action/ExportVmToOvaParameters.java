package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;

public class ExportVmToOvaParameters extends ExportOvaParameters {

    public enum Phase {
        CREATE_SNAPSHOT,
        CREATE_OVA,
        REMOVE_SNAPSHOT
    }

    private Phase phase = Phase.CREATE_SNAPSHOT;
    private Guid snapshotId;

    public ExportVmToOvaParameters() {
        setEntityType(VmEntityType.VM);
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }
}
