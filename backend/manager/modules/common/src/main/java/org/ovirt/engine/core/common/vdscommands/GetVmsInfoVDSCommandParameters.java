package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class GetVmsInfoVDSCommandParameters extends StorageDomainIdParametersBase {

    private java.util.ArrayList<Guid> privateVmIdList;

    public java.util.ArrayList<Guid> getVmIdList() {
        return privateVmIdList;
    }

    public void setVmIdList(java.util.ArrayList<Guid> value) {
        privateVmIdList = value;
    }

    public GetVmsInfoVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetVmsInfoVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmIdList = %s", super.toString(), getVmIdList());
    }
}
