package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CreateOvfVolumeForStorageDomainCommandParameters extends StorageDomainParametersBase {
    private boolean skipDomainChecks;

    public CreateOvfVolumeForStorageDomainCommandParameters() {
        super();
    }

    public CreateOvfVolumeForStorageDomainCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public boolean isSkipDomainChecks() {
        return skipDomainChecks;
    }

    public void setSkipDomainChecks(boolean skipDomainChecks) {
        this.skipDomainChecks = skipDomainChecks;
    }
}
