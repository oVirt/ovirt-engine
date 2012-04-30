package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmTemplateInterfaceParameters extends VmTemplateParametersBase {

    private static final long serialVersionUID = 3492037768582415400L;
    @Valid
    private VmNetworkInterface _interface;

    public VmNetworkInterface getInterface() {
        return _interface;
    }

    public RemoveVmTemplateInterfaceParameters() {

    }

    public RemoveVmTemplateInterfaceParameters(Guid vmtId, Guid ifaceId) {
        super(vmtId);
        _interface = new VmNetworkInterface();
        _interface.setId(ifaceId);
    }
}
