package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugDiskVDSParameters extends VdsAndVmIDVDSParametersBase {

    private Disk disk;
    private VmDevice vmDevice;

    public HotPlugDiskVDSParameters() {
    }

    public HotPlugDiskVDSParameters(Guid vdsId, Guid vmId, Disk disk, VmDevice vmDevice) {
        super(vdsId, vmId);
        this.disk = disk;
        this.vmDevice = vmDevice;
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

    @Override
    public String toString() {
        return String.format("%s, volumeId = %s", super.toString(), disk.getId());
    }
}
