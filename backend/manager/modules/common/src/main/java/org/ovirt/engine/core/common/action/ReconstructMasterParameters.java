package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ReconstructMasterParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = -640521915810322901L;

    private Guid privateNewMasterDomainId;
    private boolean canChooseInactiveDomainAsMaster;
    private boolean canChooseCurrentMasterAsNewMaster;

    public ReconstructMasterParameters() {
        privateNewMasterDomainId = Guid.Empty;
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isInactive) {
        super(storageDomainId, storagePoolId);
        privateNewMasterDomainId = Guid.Empty;
        setInactive(isInactive);
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isInactive, boolean canChooseInactiveDomainAsMaster, boolean canChooseCurrentMasterAsNewMaster) {
        this(storagePoolId, storageDomainId, isInactive);
        setCanChooseInactiveDomainAsMaster(canChooseInactiveDomainAsMaster);
        setCanChooseCurrentMasterAsNewMaster(canChooseCurrentMasterAsNewMaster);
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid newMasterDomainId) {
        this(storagePoolId, Guid.Empty, false);
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

    public boolean isCanChooseCurrentMasterAsNewMaster() {
        return canChooseCurrentMasterAsNewMaster;
    }

    public void setCanChooseCurrentMasterAsNewMaster(boolean canChooseCurrentMasterAsNewMaster) {
        this.canChooseCurrentMasterAsNewMaster = canChooseCurrentMasterAsNewMaster;
    }
}
