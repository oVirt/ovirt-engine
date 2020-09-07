package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class MeasureVolumeVDSCommandParameters extends GetVolumeInfoVDSCommandParameters {
    private int dstVolFormat;
    private boolean withBacking = true;

    public MeasureVolumeVDSCommandParameters() {
    }

    public MeasureVolumeVDSCommandParameters(
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            int dstVolFormat) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.dstVolFormat = dstVolFormat;
    }

    public int getDstVolFormat() {
        return dstVolFormat;
    }

    public void setDstVolFormat(int dstVolFormat) {
        this.dstVolFormat = dstVolFormat;
    }

    public boolean isWithBacking() {
        return withBacking;
    }

    public void setWithBacking(boolean withBacking) {
        this.withBacking = withBacking;
    }
}
