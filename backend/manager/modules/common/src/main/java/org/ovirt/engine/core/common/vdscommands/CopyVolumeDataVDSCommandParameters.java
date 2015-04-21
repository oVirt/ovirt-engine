package org.ovirt.engine.core.common.vdscommands;


import org.ovirt.engine.core.common.businessentities.LocationInfo;

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
    public String toString() {
        return "CopyVolumeDataVDSCommandParameters{" +
                "srcInfo=" + srcInfo +
                ", dstInfo=" + dstInfo +
                ", collapse=" + collapse +
                '}';
    }
}
