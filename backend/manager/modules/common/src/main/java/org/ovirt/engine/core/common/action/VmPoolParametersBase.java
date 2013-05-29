package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -4244908570752388901L;
    private Guid _vmPoolId;

    public VmPoolParametersBase(Guid vmPoolId) {
        _vmPoolId = vmPoolId;
    }

    public Guid getVmPoolId() {
        return _vmPoolId;
    }

    public void setVmPoolId(Guid value) {
        _vmPoolId = value;
    }

    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public VmPoolParametersBase() {
    }
}
