package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmParameters extends MoveVmParameters implements Serializable {
    private static final long serialVersionUID = -6514416097090370831L;

    @Valid
    private VM _vm;
    private Guid _sourceDomainId;
    private Guid _destDomainId;
    private Guid _vdsGroupId;

    public ImportVmParameters() {
        _sourceDomainId = Guid.Empty;
        _destDomainId = Guid.Empty;
    }

    public ImportVmParameters(VM vm, Guid sourceStorageDomainId, Guid destStorageDomainId, Guid storagePoolId,
            Guid vdsGroupId) {
        super(vm.getId(), destStorageDomainId);
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

    public void setVm(VM vm) {
        _vm = vm;
    }

    public Guid getSourceDomainId() {
        return _sourceDomainId;
    }

    public Guid getDestDomainId() {
        return _destDomainId;
    }

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        _vdsGroupId = value;
    }

}
