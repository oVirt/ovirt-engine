package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class ReconstructMasterParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = -640521915810322901L;

    private Guid privateNewMasterDomainId = Guid.Empty;

    public ReconstructMasterParameters() {
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isInactive) {
        super(storageDomainId, storagePoolId);
        setInactive(isInactive);
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid newMasterDomainId) {
        this(storagePoolId, Guid.Empty, false);
        this.privateNewMasterDomainId = newMasterDomainId;
    }

    public Guid getNewMasterDomainId() {
        return privateNewMasterDomainId;
    }

    public void setNewMasterDomainId(Guid value) {
        privateNewMasterDomainId = value;
    }
}
