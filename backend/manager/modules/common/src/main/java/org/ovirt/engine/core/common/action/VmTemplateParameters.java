package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmTemplateParameters extends ActionParametersBase implements Serializable {

    private static final long serialVersionUID = -8930994274659598061L;

    private Guid vmTemplateId;

    public VmTemplateParameters() {
        this(Guid.Empty);
    }

    public VmTemplateParameters(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

}
