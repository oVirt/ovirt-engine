package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StopVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -1331508207367552128L;
    private StopVmTypeEnum _stopVmType;

    public StopVmParameters(Guid vmID, StopVmTypeEnum stopVmType) {
        super(vmID);
        _stopVmType = stopVmType;
    }

    public StopVmTypeEnum getStopVmType() {
        return _stopVmType;
    }

    public void setStopVmType(StopVmTypeEnum value) {
        _stopVmType = value;
    }

    public StopVmParameters() {
        _stopVmType = StopVmTypeEnum.NORMAL;
    }
}
