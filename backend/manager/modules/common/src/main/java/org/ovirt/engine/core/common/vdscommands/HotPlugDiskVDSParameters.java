package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;

import java.util.Map;

public class HotPlugDiskVDSParameters extends VdsAndVmIDVDSParametersBase {

    private Disk disk;
    private VmDevice vmDevice;
    private VM vm;
    private Map<String, String> addressMap;

    public HotPlugDiskVDSParameters() {
    }

    public HotPlugDiskVDSParameters(Guid vdsId, VM vm, Disk disk, VmDevice vmDevice, Map<String, String> addressMap) {
        super(vdsId, vm.getId());
        this.disk = disk;
        this.vmDevice = vmDevice;
        this.vm = vm;
        this.addressMap = addressMap;
    }

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }

    public void setVmDevice(VmDevice vmDevice) {
        this.vmDevice = vmDevice;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public Map<String, String> getAddressMap() {
        return addressMap;
    }

    public void setAddressMap(Map<String, String> addressMap) {
        this.addressMap = addressMap;
    }

    @Override
    public String toString() {
        return String.format("%s, diskId = %s, addressMap = %s", super.toString(), disk.getId(), getAddressMap());
    }
}
