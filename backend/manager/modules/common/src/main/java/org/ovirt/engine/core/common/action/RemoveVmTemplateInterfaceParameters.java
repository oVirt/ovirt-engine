package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmTemplateInterfaceParameters extends VmTemplateParameters {

    private static final long serialVersionUID = 3492037768582415400L;
    private Guid interfaceId;

    public Guid getInterfaceId() {
        return interfaceId;
    }

    public RemoveVmTemplateInterfaceParameters() {

    }

    public RemoveVmTemplateInterfaceParameters(Guid vmtId, Guid ifaceId) {
        super(vmtId);
        interfaceId = ifaceId;
    }
}
