package org.ovirt.engine.core.common.vdscommands;

import java.util.Arrays;

import org.ovirt.engine.core.compat.Guid;

public class GetDevicesVisibilityVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private String[] devicesIds;

    public GetDevicesVisibilityVDSCommandParameters(Guid vdsId, String[] devicesIds) {
        super(vdsId);
        this.devicesIds = devicesIds;
    }

    public GetDevicesVisibilityVDSCommandParameters() {
    }

    public String[] getDevicesIds() {
        return devicesIds;
    }

    public void setDevicesIds(String[] devicesIds) {
        this.devicesIds = devicesIds;
    }

    @Override
    public String toString() {
        return String.format("%s, devicesIds=%s", super.toString(), Arrays.toString(getDevicesIds()));
    }

}
