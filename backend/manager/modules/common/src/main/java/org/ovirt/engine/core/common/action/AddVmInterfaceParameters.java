package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmInterfaceParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -816057026613138800L;
    @Valid
    private VmNetworkInterface _interface;

    public AddVmInterfaceParameters(Guid vmId, VmNetworkInterface iface) {
        super(vmId);
        _interface = iface;
    }

    public VmNetworkInterface getInterface() {
        return _interface;
    }

    public AddVmInterfaceParameters() {
    }
}
