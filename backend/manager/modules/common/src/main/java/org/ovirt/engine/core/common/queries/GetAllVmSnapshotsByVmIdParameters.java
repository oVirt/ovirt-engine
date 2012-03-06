package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllVmSnapshotsByVmIdParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -1357252664326554046L;

    private Guid vmId = new Guid();

    public GetAllVmSnapshotsByVmIdParameters() {
    }

    public GetAllVmSnapshotsByVmIdParameters(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }
}
