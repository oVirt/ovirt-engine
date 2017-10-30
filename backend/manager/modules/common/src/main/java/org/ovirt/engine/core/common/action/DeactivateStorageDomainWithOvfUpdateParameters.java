package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class DeactivateStorageDomainWithOvfUpdateParameters extends StorageDomainPoolParametersBase {

    private static final long serialVersionUID = 6993493855631667397L;

    private boolean forceMaintenance;

    public DeactivateStorageDomainWithOvfUpdateParameters() {
    }

    public DeactivateStorageDomainWithOvfUpdateParameters(Guid storageId, Guid storagePoolId, boolean forceMaintenance) {
        super(storageId, storagePoolId);
        setForceMaintenance(forceMaintenance);
    }

    public boolean isForceMaintenance() {
        return forceMaintenance;
    }

    public void setForceMaintenance(boolean forceMaintenance) {
        this.forceMaintenance = forceMaintenance;
    }
}
