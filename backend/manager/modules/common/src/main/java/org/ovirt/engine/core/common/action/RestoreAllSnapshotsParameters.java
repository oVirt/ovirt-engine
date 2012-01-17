package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RestoreAllSnapshotsParameters")
public class RestoreAllSnapshotsParameters extends TryBackToAllSnapshotsOfVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = -8756081739745132849L;

    public RestoreAllSnapshotsParameters(Guid vmId, Guid dstSnapshotId) {
        super(vmId, dstSnapshotId);
    }

    @XmlElement(name = "ImagesList")
    private List<DiskImage> privateImagesList;

    public List<DiskImage> getImagesList() {
        return privateImagesList;
    }

    public void setImagesList(List<DiskImage> value) {
        privateImagesList = value;
    }

    public RestoreAllSnapshotsParameters() {
    }
}
