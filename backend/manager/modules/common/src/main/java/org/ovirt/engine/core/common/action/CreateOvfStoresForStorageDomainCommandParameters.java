package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateOvfStoresForStorageDomainCommandParameters extends StorageDomainParametersBase {
    private int storesCount;
    private boolean skipDomainChecks;

    public CreateOvfStoresForStorageDomainCommandParameters() {
    }

    public CreateOvfStoresForStorageDomainCommandParameters(Guid storagePoolId, Guid storageDomainId, int storesCount) {
        super(storagePoolId, storageDomainId);
        this.storesCount = storesCount;
    }

    public int getStoresCount() {
        return storesCount;
    }

    public void setStoresCount(int storesCount) {
        this.storesCount = storesCount;
    }

    public boolean isSkipDomainChecks() {
        return skipDomainChecks;
    }

    public void setSkipDomainChecks(boolean skipDomainChecks) {
        this.skipDomainChecks = skipDomainChecks;
    }
}
