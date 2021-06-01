package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SealVmParameters extends VmOperationParameterBase implements HostJobCommandParameters {

    private static final long serialVersionUID = -1553130601745728175L;

    private Guid hostJobId;

    public SealVmParameters() {
    }

    @Override
    public Guid getHostJobId() {
        return hostJobId;
    }

    public void setHostJobId(Guid hostJobId) {
        this.hostJobId = hostJobId;
    }

}
