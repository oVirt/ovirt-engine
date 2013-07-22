package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;

public class VmNicDeviceVDSParameters extends VdsIdVDSCommandParametersBase {

    private VM vm;
    private VmNic nic;
    private VmDevice vmDevice;

    public VmNicDeviceVDSParameters(Guid vdsId, VM vm, VmNic nic, VmDevice vmDevice) {
        super(vdsId);
        this.vm = vm;
        this.nic = nic;
        this.vmDevice = vmDevice;
    }

    public VM getVm() {
        return vm;
    }

    public VmNic getNic() {
        return nic;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }

    @Override
    public String toString() {
        return String.format("%s, vm.vm_name=%s, nic=%s, vmDevice=%s",
                super.toString(),
                (getVm() == null ? "" : getVm().getName()),
                getNic(),
                getVmDevice());
    }
}
