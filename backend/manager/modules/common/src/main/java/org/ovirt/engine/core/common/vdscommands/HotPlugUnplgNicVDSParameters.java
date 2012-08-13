package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugUnplgNicVDSParameters extends VdsIdVDSCommandParametersBase {

    private final VM vm;
    private final VmNetworkInterface nic;
    private final VmDevice vmDevice;

    public HotPlugUnplgNicVDSParameters(Guid vdsId, VM vm, VmNetworkInterface nic, VmDevice vmDevice) {
        super(vdsId);
        this.vm = vm;
        this.nic = nic;
        this.vmDevice = vmDevice;
    }

    public VM getVm() {
        return vm;
    }

    public VmNetworkInterface getNic() {
        return nic;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }
}
