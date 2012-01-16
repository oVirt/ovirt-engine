package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class RemoveSnapshotParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -2684524270498397962L;

    private Guid _sourceVmSnapshotId = new Guid();

    private NGuid _destVmSnapshotId;

    public RemoveSnapshotParameters(Guid sourceVmSnapshotId, NGuid destVmSnapshotId, Guid vmGuid) {
        super(vmGuid);
        _sourceVmSnapshotId = sourceVmSnapshotId;
        _destVmSnapshotId = destVmSnapshotId;
    }

    public Guid getSourceVmSnapshotId() {
        return _sourceVmSnapshotId;
    }

    public void setSourceVmSnapshotId(Guid value) {
        _sourceVmSnapshotId = value;
    }

    public NGuid getDestVmSnapshotId() {
        return _destVmSnapshotId;
    }

    public void setDestVmSnapshotId(NGuid value) {
        _destVmSnapshotId = value;
    }

    public RemoveSnapshotParameters() {
    }
}
