package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ChangeVMClusterParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 7078020632613403675L;
    private Guid clusterId;

    public ChangeVMClusterParameters() {
    }

    public ChangeVMClusterParameters(Guid clusterId, Guid vmId) {
        super(vmId);
        this.clusterId = clusterId;

    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

}
