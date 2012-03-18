package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;

public class AddVmPoolWithVmsParameters extends VmPoolOperationParameters {
    private static final long serialVersionUID = 4826143612049185740L;

    @Valid
    private VM _vm;
    private int _vmsCount;
    private int _diskSize;
    private HashMap<Guid, Guid> imageToDestinationDomainMap;

    public AddVmPoolWithVmsParameters() {
    }

    public AddVmPoolWithVmsParameters(vm_pools vmPool, VM vm, int count, int diskSize) {
        super(vmPool);
        _vm = vm;
        _vmsCount = count;
        _diskSize = diskSize;
    }

    @Valid
    public VmStatic getVmStaticData() {
        return _vm.getStaticData();
    }

    public int getVmsCount() {
        return _vmsCount;
    }

    public int getDiskSize() {
        return _diskSize;
    }

    public void setImageToDestinationDomainMap(HashMap<Guid, Guid> imageToDestinationDomainMap) {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    public HashMap<Guid, Guid> getImageToDestinationDomainMap() {
        return imageToDestinationDomainMap;
    }
}
