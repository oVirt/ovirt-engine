package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVmFromImportExportParamenters")
public class RemoveVmFromImportExportParamenters extends RemoveVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = 1841755064122049392L;
    @XmlElement
    private VM _vm;

    public RemoveVmFromImportExportParamenters(VM vm, Guid storageDomainId, Guid storagePoolId) {
        super(vm.getId(), false);
        _vm = vm;
        this.setStorageDomainId(storageDomainId);
        this.setStoragePoolId(storagePoolId);
    }

    public VM getVm() {
        return _vm;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public RemoveVmFromImportExportParamenters() {
    }
}
