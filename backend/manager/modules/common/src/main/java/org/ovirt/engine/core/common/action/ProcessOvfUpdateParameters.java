package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ProcessOvfUpdateParameters extends StorageDomainParametersBase {
    private boolean skipDomainChecks;
    private OvfUpdateStep ovfUpdateStep;

    public ProcessOvfUpdateParameters() {
        super();
    }

    public ProcessOvfUpdateParameters(Guid storagePoolId, Guid storageDomainId) {
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
