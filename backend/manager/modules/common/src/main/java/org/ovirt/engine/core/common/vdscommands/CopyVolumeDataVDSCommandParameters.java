package org.ovirt.engine.core.common.vdscommands;


import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CopyVolumeDataVDSCommandParameters extends StorageJobVdsCommandParameters {

    private LocationInfo srcInfo;
    private LocationInfo dstInfo;
    private boolean collapse;
    private boolean copyBitmaps;

    public CopyVolumeDataVDSCommandParameters() {
    }


    public CopyVolumeDataVDSCommandParameters(Guid jobId, LocationInfo srcInfo, LocationInfo dstInfo,
            boolean collapse, boolean copyBitmaps) {
        super(null);
        this.srcInfo = srcInfo;
        this.dstInfo = dstInfo;
        this.collapse = collapse;
        this.copyBitmaps = copyBitmaps;
        setJobId(jobId);
    }

    public LocationInfo getSrcInfo() {
        return srcInfo;
    }

    public void setSrcInfo(LocationInfo srcInfo) {
        this.srcInfo = srcInfo;
    }

    public LocationInfo getDstInfo() {
        return dstInfo;
    }

    public void setDstInfo(LocationInfo dstInfo) {
        this.dstInfo = dstInfo;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    public boolean isCopyBitmaps() {
        return copyBitmaps;
    }

    public void setCopyBitmaps(boolean copyBitmaps) {
        this.copyBitmaps = copyBitmaps;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("srcInfo", srcInfo)
                .append("dstInfo", dstInfo)
                .append("collapse", collapse)
                .append("copyBitmaps", copyBitmaps);
    }
}
