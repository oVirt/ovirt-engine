package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private String iface;
    private int index;
    private String diskPath;
    private Map<String, String> driveSpec;

    // Older implementation uses diskPath, which specifies the CD by providing path to the image. New implementation
    // uses driveSpec, which specifies the CD by PDIV. In both cases null or empty value means to ejects CD, so we
    // cannot decide based on the null value if old or new implementation is used. Therefore we need a special flag,
    // which indicates if the CD is specified by PDIV (usingPdiv == true) or by path (usingPdiv == false).
    private boolean usingPdiv;

    public ChangeDiskVDSCommandParameters() {
    }

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String diskPath) {
        this(vdsId, vmId, null, 0, diskPath, null, false);
    }

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String iface, int index, String diskPath) {
        this(vdsId, vmId, iface, index, diskPath, null, false);
    }

    public ChangeDiskVDSCommandParameters(
            Guid vdsId, Guid vmId, String iface, int index, Map<String, String> driveSpec) {
        this(vdsId, vmId, iface, index, null, driveSpec, true);
    }

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String iface, int index, String diskPath,
            Map<String, String> driveSpec, boolean usePdiv) {
        super(vdsId, vmId);
        this.iface = iface;
        this.index = index;
        this.diskPath = diskPath;
        this.usingPdiv = usePdiv;
        this.driveSpec = driveSpec;
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

    public boolean isUsingPdiv() {
        return usingPdiv;
    }

    public Map<String, String> getDriveSpec() {
        return driveSpec;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("iface", getIface())
                .append("index", getIndex())
                .append("diskPath", getDiskPath())
                .append("usingPdiv", isUsingPdiv())
                .append("driveSpec", getDriveSpec());
    }

}
