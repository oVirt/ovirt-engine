package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainPoolParametersBase extends StorageDomainParametersBase {
    private static final long serialVersionUID = 6248101174739394633L;

    private boolean runSilent;
    private boolean inactive;
    private boolean skipChecks;
    private boolean skipLock;

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean value) {
        inactive = value;
    }

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

    public boolean isSkipChecks() {
        return skipChecks;
    }

    public void setSkipChecks(boolean skipChecks) {
        this.skipChecks = skipChecks;
    }

    public boolean isSkipLock() {
        return skipLock;
    }

    public void setSkipLock(boolean skipLock) {
        this.skipLock = skipLock;
    }
}
