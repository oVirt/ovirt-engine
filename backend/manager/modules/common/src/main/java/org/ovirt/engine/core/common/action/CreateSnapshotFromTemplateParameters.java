package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateSnapshotFromTemplateParameters extends ImagesActionsParametersBase {
    private static final long serialVersionUID = -3841623510039174546L;

    private Guid privateVmId = Guid.Empty;
    private Guid destSorageDomainId;

    public CreateSnapshotFromTemplateParameters(Guid imageId, Guid vmId) {
        super(imageId);
        setVmId(vmId);
    }

    public CreateSnapshotFromTemplateParameters() {
    }

    public void setDestSorageDomainId(Guid destSorageDomainId) {
        this.destSorageDomainId = destSorageDomainId;
    }

    public Guid getDestSorageDomainId() {
        return destSorageDomainId;
    }

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }
}
