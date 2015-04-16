package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("devicesIds", getDevicesIds());
    }
}
