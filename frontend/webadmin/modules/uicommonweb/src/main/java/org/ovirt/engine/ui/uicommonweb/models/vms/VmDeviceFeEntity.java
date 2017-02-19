package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * {@link VmDevice}, its {@link VM} and frontend related properties
 * <p>
 * <p>Identity intentionally same as for VmDevice</p>
 */
public class VmDeviceFeEntity {

    private final VmDevice vmDevice;
    private final VM vm;
    private boolean beingUnplugged;

    public VmDeviceFeEntity(VmDevice vmDevice, VM vm, boolean beingUnplugged) {
        if (!vmDevice.getVmId().equals(vm.getId())) {
            throw new IllegalArgumentException("IDs of VM and device's VM doesn't match."); //$NON-NLS-1$
        }
        this.vmDevice = vmDevice;
        this.vm = vm;
        this.beingUnplugged = beingUnplugged;
    }

    public boolean isBeingUnplugged() {
        return beingUnplugged;
    }

    public void setBeingUnplugged(boolean beingUnplugged) {
        this.beingUnplugged = beingUnplugged;
    }

    public VM getVm() {
        return vm;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VmDeviceFeEntity)) {
            return false;
        }
        VmDeviceFeEntity that = (VmDeviceFeEntity) o;
        return Objects.equals(vmDevice, that.vmDevice) &&
                Objects.equals(vm, that.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmDevice, vm);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("vmDevice", vmDevice) //$NON-NLS-1$
                .append("beingUnplugged", beingUnplugged) //$NON-NLS-1$
                .toString();
    }
}
