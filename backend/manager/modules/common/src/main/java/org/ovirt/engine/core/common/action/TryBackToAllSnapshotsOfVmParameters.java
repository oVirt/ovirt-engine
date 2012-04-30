package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 1862924807826485840L;
    private Guid _dstSnapshotId = new Guid();

    public TryBackToAllSnapshotsOfVmParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId);
        _dstSnapshotId = dstSnapshotId;
    }

    public Guid getDstSnapshotId() {
        return _dstSnapshotId;
    }

    public TryBackToAllSnapshotsOfVmParameters() {
    }
}
