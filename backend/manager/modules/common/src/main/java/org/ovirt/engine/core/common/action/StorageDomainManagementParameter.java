package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainManagementParameter extends StorageDomainParametersBase {
    private static final long serialVersionUID = -4439958770559256988L;
    @Valid
    private StorageDomainStatic privateStorageDomain;

    public StorageDomainStatic getStorageDomain() {
        return privateStorageDomain;
    }

    public void setStorageDomain(StorageDomainStatic value) {
        privateStorageDomain = value;
    }

    public StorageDomainManagementParameter(StorageDomainStatic storageDomain) {
        super(storageDomain.getId());
        setStorageDomain(storageDomain);
        setVdsId(Guid.Empty);
    }

    public StorageDomainManagementParameter() {
    }
}
