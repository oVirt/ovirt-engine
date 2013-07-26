package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateInterfaceParameters extends VmTemplateParametersBase {
    private static final long serialVersionUID = 5177618608953713542L;

    @Valid
    private VmNetworkInterface nic;

    public AddVmTemplateInterfaceParameters() {
    }

    public AddVmTemplateInterfaceParameters(Guid templateId, VmNetworkInterface nic) {
        super(templateId);
        this.nic = nic;
    }

    public VmNetworkInterface getInterface() {
        return nic;
    }
}
