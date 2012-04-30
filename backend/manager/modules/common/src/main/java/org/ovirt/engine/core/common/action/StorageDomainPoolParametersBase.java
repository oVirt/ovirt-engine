package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class StorageDomainPoolParametersBase extends StorageDomainParametersBase {
    private static final long serialVersionUID = 6248101174739394633L;

    boolean runSilent;

    public StorageDomainPoolParametersBase(Guid storageId, Guid storagePoolId) {
        super(storageId);
        setStoragePoolId(storagePoolId);
    }

    public StorageDomainPoolParametersBase() {
    }

    public boolean isRunSilent() {
        return runSilent;
    }

    public void setRunSilent(boolean runSilent) {
        this.runSilent = runSilent;
    }
}
