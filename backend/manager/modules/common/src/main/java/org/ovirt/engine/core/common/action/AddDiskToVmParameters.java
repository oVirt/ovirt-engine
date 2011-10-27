package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddDiskToVmParameters")
public class AddDiskToVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -7832310521101821905L;

    public AddDiskToVmParameters(Guid vmId, DiskImageBase diskInfo) {
        super(vmId);
        setDiskInfo(diskInfo);
    }

    @Valid
    @XmlElement(name = "DiskInfo")
    private DiskImageBase privateDiskInfo;

    public DiskImageBase getDiskInfo() {
        return privateDiskInfo;
    }

    public void setDiskInfo(DiskImageBase value) {
        privateDiskInfo = value;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement
    private Guid privateVmSnapshotId = new Guid();

    public Guid getVmSnapshotId() {
        return privateVmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        privateVmSnapshotId = value;
    }

    public AddDiskToVmParameters() {
    }
}
