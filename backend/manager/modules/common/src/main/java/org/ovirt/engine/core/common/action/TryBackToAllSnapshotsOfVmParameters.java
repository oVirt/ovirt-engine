package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TryBackToAllSnapshotsOfVmParameters")
public class TryBackToAllSnapshotsOfVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 1862924807826485840L;
    @XmlElement
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
