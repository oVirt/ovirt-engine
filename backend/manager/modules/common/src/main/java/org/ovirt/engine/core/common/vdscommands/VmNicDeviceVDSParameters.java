package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
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

    public VmNicDeviceVDSParameters() {
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vm.vm_name", getVm() == null ? "" : getVm().getName())
                .append("nic", getNic())
                .append("vmDevice", getVmDevice());
    }
}
