package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmParameters extends MoveVmParameters implements Serializable {
    private static final long serialVersionUID = -6514416097090370831L;

    @Valid
    private VM vm;
    private Guid sourceDomainId;
    private Guid destDomainId;
    private Guid vdsGroupId;

    public ImportVmParameters() {
        sourceDomainId = Guid.Empty;
        destDomainId = Guid.Empty;
    }

    public ImportVmParameters(VM vm, Guid sourceStorageDomainId, Guid destStorageDomainId, Guid storagePoolId,
            Guid vdsGroupId) {
        super(vm.getId(), destStorageDomainId);
        this.sourceDomainId = sourceStorageDomainId;
        this.destDomainId = destStorageDomainId;
        setVm(vm);
        setStorageDomainId(destStorageDomainId);
        setStoragePoolId(storagePoolId);
        setVdsGroupId(vdsGroupId);
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public Guid getSourceDomainId() {
        return sourceDomainId;
    }

    public Guid getDestDomainId() {
        return destDomainId;
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid vdsGroupId) {
        this.vdsGroupId = vdsGroupId;
    }

}
