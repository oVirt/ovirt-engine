package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class StopVmParameters extends StopVmParametersBase implements Serializable {
    private static final long serialVersionUID = -1331508207367552128L;
    private StopVmTypeEnum stopVmType;

    public StopVmParameters() {
        stopVmType = StopVmTypeEnum.NORMAL;
    }

    public StopVmParameters(Guid vmId, StopVmTypeEnum stopVmType) {
        super(vmId);
        this.stopVmType = stopVmType;
    }

    public StopVmParameters(Guid vmId, StopVmTypeEnum stopVmType, String reason) {
        this(vmId, stopVmType);
        setStopReason(reason);
    }

    public StopVmParameters(Guid vmId, StopVmTypeEnum stopVmType, String reason, boolean forceStop) {
        this(vmId, stopVmType, reason);
        setForceStop(forceStop);
    }

    public StopVmTypeEnum getStopVmType() {
        return stopVmType;
    }

    public void setStopVmType(StopVmTypeEnum value) {
        stopVmType = value;
    }

}
