package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SealVmTemplateParameters extends VmTemplateParameters implements HostJobCommandParameters {

    private static final long serialVersionUID = -3939385513268425835L;

    private Guid hostJobId;

    public SealVmTemplateParameters() {
    }

    @Override
    public Guid getHostJobId() {
        return hostJobId;
    }

    public void setHostJobId(Guid hostJobId) {
        this.hostJobId = hostJobId;
    }

}
