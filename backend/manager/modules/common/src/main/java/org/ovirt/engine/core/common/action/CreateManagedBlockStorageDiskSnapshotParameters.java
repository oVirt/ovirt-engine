package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateManagedBlockStorageDiskSnapshotParameters extends ImagesContainterParametersBase {
    private static final long serialVersionUID = 5236716449335804793L;

    private Guid volumeId;
    private Guid vmId;
    private Guid storageDomainId;

    public CreateManagedBlockStorageDiskSnapshotParameters() {
    }

    public CreateManagedBlockStorageDiskSnapshotParameters(Guid volumeId, Guid vmId, Guid storageDomainId) {
        this.volumeId = volumeId;
        this.vmId = vmId;
        this.storageDomainId = storageDomainId;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
