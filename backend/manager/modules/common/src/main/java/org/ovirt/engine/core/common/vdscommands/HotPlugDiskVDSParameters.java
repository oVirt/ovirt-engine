package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugDiskVDSParameters extends VdsAndVmIDVDSParametersBase {

    private Disk disk;
    private VmDevice vmDevice;
    private VM vm;
    private DiskInterface diskInterface;
    private boolean passDiscard;

    public HotPlugDiskVDSParameters() {
    }

    public HotPlugDiskVDSParameters(Guid vdsId, VM vm, Disk disk, VmDevice vmDevice, DiskInterface diskInterface,
            boolean passDiscard) {
        super(vdsId, vm.getId());
        this.disk = disk;
        this.vmDevice = vmDevice;
        this.vm = vm;
        this.diskInterface = diskInterface;
        this.passDiscard = passDiscard;
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

    public DiskInterface getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(DiskInterface diskInterface) {
        this.diskInterface = diskInterface;
    }

    public boolean isPassDiscard() {
        return passDiscard;
    }

    public void setPassDiscard(boolean passDiscard) {
        this.passDiscard = passDiscard;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("diskId", disk.getId());
    }
}
