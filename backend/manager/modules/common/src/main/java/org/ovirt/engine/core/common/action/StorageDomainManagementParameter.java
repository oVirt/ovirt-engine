package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainManagementParameter extends StorageDomainParametersBase {
    private static final long serialVersionUID = -4439958770559256988L;
    @Valid
    private storage_domain_static privateStorageDomain;

    public storage_domain_static getStorageDomain() {
        return privateStorageDomain;
    }

    private void setStorageDomain(storage_domain_static value) {
        privateStorageDomain = value;
    }

    public StorageDomainManagementParameter(storage_domain_static storageDomain) {
        super(storageDomain.getId());
        setStorageDomain(storageDomain);
        setVdsId(Guid.Empty);
    }

    public StorageDomainManagementParameter() {
    }
}
