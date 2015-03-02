package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolParametersBase extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = -4244908570752388901L;
    private Guid vmPoolId;
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

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
