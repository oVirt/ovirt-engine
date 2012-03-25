package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class HotPlugUnplgNicVDSParameters extends VdsIdVDSCommandParametersBase {

    private final Guid vmId;
    private final VmNetworkInterface nic;
    private final VmDevice vmDevice;

    public HotPlugUnplgNicVDSParameters(Guid vdsId, Guid vmId, VmNetworkInterface nic, VmDevice vmDevice) {
        super(vdsId);
        this.vmId = vmId;
        this.nic = nic;
        this.vmDevice = vmDevice;
    }

    public Guid getVmId() {
        return vmId;
    }

    public VmNetworkInterface getNic() {
        return nic;
    }

    public VmDevice getVmDevice() {
        return vmDevice;
    }

}
