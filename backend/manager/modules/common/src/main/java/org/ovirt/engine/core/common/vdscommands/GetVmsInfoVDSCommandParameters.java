package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetVmsInfoVDSCommandParameters extends StorageDomainIdParametersBase {

    private ArrayList<Guid> privateVmIdList;

    public ArrayList<Guid> getVmIdList() {
        return privateVmIdList;
    }

    public void setVmIdList(ArrayList<Guid> value) {
        privateVmIdList = value;
    }

    public GetVmsInfoVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetVmsInfoVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmIdList", getVmIdList());
    }
}
