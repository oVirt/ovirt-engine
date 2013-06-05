package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmFromImportExportParamenters extends RemoveVmParameters implements java.io.Serializable {
    private static final long serialVersionUID = 1841755064122049392L;
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

    private Guid privateStorageDomainId = Guid.Empty;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    private Guid privateStoragePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public RemoveVmFromImportExportParamenters() {
    }
}
