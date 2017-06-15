package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolParametersBase extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -4244908570752388901L;
    private Guid vmPoolId;
    private String vmPoolName;
    private Guid storageDomainId;

    public VmPoolParametersBase() {
        storageDomainId = Guid.Empty;
    }

    public VmPoolParametersBase(Guid vmPoolId) {
        this();
        setVmPoolId(vmPoolId);
    }

    public Guid getVmPoolId() {
        return vmPoolId;
    }

    public void setVmPoolId(Guid vmPoolId) {
        this.vmPoolId = vmPoolId;
    }

    public String getVmPoolName() {
        return vmPoolName;
    }

    public void setVmPoolName(String vmPoolName) {
        this.vmPoolName = vmPoolName;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
