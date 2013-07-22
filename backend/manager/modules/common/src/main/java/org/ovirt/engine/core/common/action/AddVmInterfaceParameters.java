package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmInterfaceParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -816057026613138800L;

    @Valid
    private VmNetworkInterface nic;

    public AddVmInterfaceParameters() {
    }

    public AddVmInterfaceParameters(Guid vmId, VmNetworkInterface iface) {
        super(vmId);
        nic = iface;
    }

    public VmNetworkInterface getInterface() {
        return nic;
    }
}
