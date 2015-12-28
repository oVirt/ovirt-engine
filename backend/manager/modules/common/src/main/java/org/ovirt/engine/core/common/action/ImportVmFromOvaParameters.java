package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromOvaParameters extends ImportVmFromExternalProviderParameters {
    private static final long serialVersionUID = 904072368682302777L;

    private String ovaPath;

    public ImportVmFromOvaParameters() {
    }

    public ImportVmFromOvaParameters(VM vm, Guid destStorageDomainId, Guid storagePoolId, Guid clusterId) {
        super(vm, destStorageDomainId, storagePoolId, clusterId);
    }

    public String getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }
}
