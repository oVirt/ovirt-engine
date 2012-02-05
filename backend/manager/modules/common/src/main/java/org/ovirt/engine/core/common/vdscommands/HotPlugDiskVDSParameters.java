package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugDiskVDSParameters extends VdsAndVmIDVDSParametersBase {

    private DiskImage diskImage;
    private VmDevice vmDevice;

    public HotPlugDiskVDSParameters() {
    }

    public HotPlugDiskVDSParameters(Guid vdsId, Guid vmId, DiskImage diskImage, VmDevice vmDevice) {
        super(vdsId, vmId);
        this.diskImage = diskImage;
        this.vmDevice = vmDevice;
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage diskImage) {
        this.diskImage = diskImage;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }

    public void setVmDevice(VmDevice vmDevice) {
        this.vmDevice = vmDevice;
    }

    @Override
    public String toString() {
        return String.format("%s, volumeId = %s", super.toString(), diskImage.getId());
    }
}
