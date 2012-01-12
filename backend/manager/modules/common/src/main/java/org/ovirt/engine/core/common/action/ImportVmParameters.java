package org.ovirt.engine.core.common.action;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImportVmParameters")
public class ImportVmParameters extends MoveVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = -6514416097090370831L;

    @XmlElement
    private VM _vm;

    @XmlElement
    private Guid _sourceDomainId = new Guid();

    @XmlElement
    private Guid _destDomainId = new Guid();

    @XmlElement
    private List<DiskImage> _images;

    @XmlElement
    private Guid _vdsGroupId;

    public ImportVmParameters() {
    }

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

    public List<DiskImage> getImages() {
        return _images == null ? Collections.<DiskImage>emptyList() : _images;
    }

    public void setImages(List<DiskImage> value) {
        _images = value;
    }

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        _vdsGroupId = value;
    }

}
