package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmPoolParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -4244908570752388901L;
    private NGuid _vmPoolId;

    public VmPoolParametersBase(NGuid vmPoolId) {
        _vmPoolId = vmPoolId;
    }

    public NGuid getVmPoolId() {
        return _vmPoolId;
    }

    public void setVmPoolId(NGuid value) {
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
