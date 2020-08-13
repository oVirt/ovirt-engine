package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateSnapshotFromTemplateParameters extends CreateSnapshotParameters {
    private static final long serialVersionUID = -3841623510039174546L;

    private Guid privateVmId;
    private Guid destStorageDomainId;

    public CreateSnapshotFromTemplateParameters(Guid imageId, Guid vmId) {
        super(imageId);
        setVmId(vmId);
    }

    public CreateSnapshotFromTemplateParameters() {
        privateVmId = Guid.Empty;
    }

    public void setDestStorageDomainId(Guid destSorageDomainId) {
        this.destStorageDomainId = destSorageDomainId;
    }

    public Guid getDestStorageDomainId() {
        return destStorageDomainId;
    }

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }
}
