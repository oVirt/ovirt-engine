package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ProcessOvfUpdateForStorageDomainCommandParameters extends StorageDomainParametersBase {
    private boolean skipDomainChecks;
    private OvfUpdateStep ovfUpdateStep;

    public ProcessOvfUpdateForStorageDomainCommandParameters() {
        super();
    }

    public ProcessOvfUpdateForStorageDomainCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public boolean isSkipDomainChecks() {
        return skipDomainChecks;
    }

    public void setSkipDomainChecks(boolean skipDomainChecks) {
        this.skipDomainChecks = skipDomainChecks;
    }


    public OvfUpdateStep getOvfUpdateStep() {
        return ovfUpdateStep;
    }

    public void setOvfUpdateStep(OvfUpdateStep ovfUpdateStep) {
        this.ovfUpdateStep = ovfUpdateStep;
    }

    public enum OvfUpdateStep {
        OVF_STORES_CREATION, OVF_UPLOAD;
    }
}
