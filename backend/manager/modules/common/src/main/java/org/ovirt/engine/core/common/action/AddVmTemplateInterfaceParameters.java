package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateInterfaceParameters extends VmTemplateParametersBase {
    private static final long serialVersionUID = 5177618608953713542L;

    @Valid
    private VmNetworkInterface _interface;

    public AddVmTemplateInterfaceParameters(Guid vmtId, VmNetworkInterface iface) {
        super(vmtId);
        _interface = iface;
    }

    public VmNetworkInterface getInterface() {
        return _interface;
    }

    public AddVmTemplateInterfaceParameters() {
    }
}
