package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class StopVmParameters extends StopVmParametersBase implements Serializable {
    private static final long serialVersionUID = -1331508207367552128L;
    private StopVmTypeEnum stopVmType;

    public StopVmParameters() {
        stopVmType = StopVmTypeEnum.NORMAL;
    }

    public StopVmParameters(Guid vmID, StopVmTypeEnum stopVmType) {
        this(vmID, stopVmType, "");
    }

    public StopVmParameters(Guid vmID, StopVmTypeEnum stopVmType, String reason) {
        super(vmID);
        this.stopVmType = stopVmType;
        setStopReason(reason);
    }

    public StopVmTypeEnum getStopVmType() {
        return stopVmType;
    }

    public void setStopVmType(StopVmTypeEnum value) {
        stopVmType = value;
    }

}
