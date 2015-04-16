package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HostDevChangeNumVfsVDSParameters extends VdsIdVDSCommandParametersBase {

    private String deviceName;
    private int numOfVfs;

    public HostDevChangeNumVfsVDSParameters(Guid vdsId, String deviceName, int numOfVfs) {
        super(vdsId);
        this.deviceName = deviceName;
        this.numOfVfs = numOfVfs;
    }

    public HostDevChangeNumVfsVDSParameters() {
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getNumOfVfs() {
        return numOfVfs;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("deviceName", getDeviceName())
                .append("numOfVfs", getNumOfVfs());
    }
}
