package org.ovirt.engine.core.common.vdscommands;


import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class CopyVolumeDataVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private LocationInfo srcInfo;
    private LocationInfo dstInfo;
    private boolean collapse;

    public CopyVolumeDataVDSCommandParameters() {
    }


    public CopyVolumeDataVDSCommandParameters(LocationInfo srcInfo, LocationInfo dstInfo, boolean collapse) {
        super(null);
        this.srcInfo = srcInfo;
        this.dstInfo = dstInfo;
        this.collapse = collapse;
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

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("srcInfo", srcInfo).append("dstInfo", dstInfo).append("collapse", collapse);
    }
}
