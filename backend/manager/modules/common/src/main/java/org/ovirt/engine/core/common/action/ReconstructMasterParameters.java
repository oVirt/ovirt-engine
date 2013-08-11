package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ReconstructMasterParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = -640521915810322901L;

    private Guid privateNewMasterDomainId;
    private boolean canChooseInactiveDomainAsMaster;

    public ReconstructMasterParameters() {
        privateNewMasterDomainId = Guid.Empty;
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isInactive) {
        super(storageDomainId, storagePoolId);
        setInactive(isInactive);
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isInactive, boolean canChooseInactiveDomainAsMaster) {
        super(storageDomainId, storagePoolId);
        setInactive(isInactive);
        setCanChooseInactiveDomainAsMaster(canChooseInactiveDomainAsMaster);
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid newMasterDomainId) {
        this(storagePoolId, Guid.Empty, false);
        privateNewMasterDomainId = Guid.Empty;
        this.privateNewMasterDomainId = newMasterDomainId;
    }

    public boolean isCanChooseInactiveDomainAsMaster() {
        return canChooseInactiveDomainAsMaster;
    }

    public void setCanChooseInactiveDomainAsMaster(boolean canChooseInactiveDomainAsMaster) {
        this.canChooseInactiveDomainAsMaster = canChooseInactiveDomainAsMaster;
    }

    public Guid getNewMasterDomainId() {
        return privateNewMasterDomainId;
    }

    public void setNewMasterDomainId(Guid value) {
        privateNewMasterDomainId = value;
    }
}
