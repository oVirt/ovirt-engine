package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class ConvertManagedBlockVolumeVDSCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private Guid storageDomainId;
    private Guid srcVolId;
    private Guid dstVolId;
    private String srcFormat;
    private String dstFormat;

    public ConvertManagedBlockVolumeVDSCommandParameters() {
    }

    public ConvertManagedBlockVolumeVDSCommandParameters(VDS vds) {
        super(vds);
    }

    public ConvertManagedBlockVolumeVDSCommandParameters(VDS vds, Guid storageDomainId,
            Guid srcVolId, Guid dstVolId, String srcFormat, String dstFormat) {
        super(vds);
        this.storageDomainId = storageDomainId;
        this.srcVolId = srcVolId;
        this.dstVolId = dstVolId;
        this.srcFormat = srcFormat;
        this.dstFormat = dstFormat;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getSrcVolId() {
        return srcVolId;
    }

    public void setSrcVolId(Guid srcVolId) {
        this.srcVolId = srcVolId;
    }

    public Guid getDstVolId() {
        return dstVolId;
    }

    public void setDstVolId(Guid dstVolId) {
        this.dstVolId = dstVolId;
    }

    public String getSrcFormat() {
        return srcFormat;
    }

    public void setSrcFormat(String srcFormat) {
        this.srcFormat = srcFormat;
    }

    public String getDstFormat() {
        return dstFormat;
    }

    public void setDstFormat(String dstFormat) {
        this.dstFormat = dstFormat;
    }
}
