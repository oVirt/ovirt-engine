package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportVmParameters")
public class ImportVmParameters extends MoveVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = -8952177290494146953L;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private VM _vm;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private Guid _sourceDomainId = new Guid();
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private Guid _destDomainId = new Guid();
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private java.util.ArrayList<DiskImage> _images;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private Guid _vdsGroupId;

    public ImportVmParameters(VM vm, Guid sourceStorageDomainId, Guid destStorageDomainId, Guid storagePoolId,
            Guid vdsGroupId) {
        super(vm.getvm_guid(), destStorageDomainId);
        _vm = vm;
        _sourceDomainId = sourceStorageDomainId;
        _destDomainId = destStorageDomainId;
        setStorageDomainId(destStorageDomainId);
        setStoragePoolId(storagePoolId);
        _vdsGroupId = vdsGroupId;
    }

    public VM getVm() {
        return _vm;
    }

    public Guid getSourceDomainId() {
        return _sourceDomainId;
    }

    public Guid getDestDomainId() {
        return _destDomainId;
    }

    public java.util.ArrayList<DiskImage> getImages() {
        return _images == null ? new ArrayList<DiskImage>() : _images;
    }

    public void setImages(java.util.ArrayList<DiskImage> value) {
        _images = value;
    }

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        _vdsGroupId = value;
    }

    public ImportVmParameters() {
    }
}
