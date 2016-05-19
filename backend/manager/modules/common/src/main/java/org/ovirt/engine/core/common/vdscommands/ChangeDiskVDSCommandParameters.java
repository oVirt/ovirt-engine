package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private String iface;
    private int index;
    private String diskPath;

    public ChangeDiskVDSCommandParameters() {
    }

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String diskPath) {
        this(vdsId, vmId, null, 0, diskPath);
    }

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String iface, int index, String diskPath) {
        super(vdsId, vmId);
        this.iface = iface;
        this.index = index;
        this.diskPath = diskPath;
    }

    public String getIface() {
        return iface;
    }

    public int getIndex() {
        return index;
    }

    public String getDiskPath() {
        return diskPath;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("iface", getIface())
                .append("index", getIndex())
                .append("diskPath", getDiskPath());
    }

}
