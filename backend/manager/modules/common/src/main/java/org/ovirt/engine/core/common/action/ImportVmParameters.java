package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmParameters extends MoveVmParameters implements Serializable {
    private static final long serialVersionUID = -6514416097090370831L;

    @Valid
    private VM vm;
    private Guid sourceDomainId;
    private Guid destDomainId;
    private Guid vdsGroupId;
    private Map<Guid, Disk> diskMap;

    public ImportVmParameters() {
        sourceDomainId = Guid.Empty;
        destDomainId = Guid.Empty;
    }

    public ImportVmParameters(VM vm, Guid sourceStorageDomainId, Guid destStorageDomainId, Guid storagePoolId,
            Guid vdsGroupId) {
        super(vm.getId(), destStorageDomainId);
        sourceDomainId = sourceStorageDomainId;
        destDomainId = destStorageDomainId;
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

    public void setDestDomainId(Guid destDomainId) {
        this.destDomainId = destDomainId;
    }

    public void setSourceDomainId(Guid sourceDomainId) {
        this.sourceDomainId = sourceDomainId;
    }

    public Map<Guid, Disk> getDiskMap() {
        return diskMap;
    }

    public void setDiskMap(Map<Guid, Disk> diskMap) {
        this.diskMap = diskMap;
    }

}
