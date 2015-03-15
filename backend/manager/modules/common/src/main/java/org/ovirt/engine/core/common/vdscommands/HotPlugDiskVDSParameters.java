package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugDiskVDSParameters extends VdsAndVmIDVDSParametersBase {

    private Disk disk;
    private VmDevice vmDevice;
    private VM vm;

    public HotPlugDiskVDSParameters() {
    }

    public HotPlugDiskVDSParameters(Guid vdsId, VM vm, Disk disk, VmDevice vmDevice) {
        super(vdsId, vm.getId());
        this.disk = disk;
        this.vmDevice = vmDevice;
        this.vm = vm;
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

    @Override
    public String toString() {
        return String.format("%s, diskId = %s", super.toString(), disk.getId());
    }
}
