package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class ChangeVMClusterParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 7078020632613403675L;
    private Guid clusterId;
    private Version vmCustomCompatibilityVersion;

    public ChangeVMClusterParameters() {
    }

    public ChangeVMClusterParameters(Guid clusterId, Guid vmId, Version vmCustomCompatibilityVersion) {
        super(vmId);
        this.clusterId = clusterId;
        this.vmCustomCompatibilityVersion = vmCustomCompatibilityVersion;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Version getVmCustomCompatibilityVersion() {
        return vmCustomCompatibilityVersion;
    }

}
