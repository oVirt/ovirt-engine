package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MergeSnapshotParamenters")
public class MergeSnapshotParamenters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -2684524270498397962L;

    @XmlElement
    private Guid _sourceVmSnapshotId = new Guid();

    @XmlElement
    private NGuid _destVmSnapshotId;

    public MergeSnapshotParamenters(Guid sourceVmSnapshotId, NGuid destVmSnapshotId, Guid vmGuid) {
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

    public MergeSnapshotParamenters() {
    }
}
