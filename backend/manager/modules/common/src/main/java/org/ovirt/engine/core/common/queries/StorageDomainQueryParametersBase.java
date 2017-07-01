package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public abstract class StorageDomainQueryParametersBase extends QueryParametersBase {
    private static final long serialVersionUID = -1267869804833489615L;

    private Guid privateStorageDomainId;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    private void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public StorageDomainQueryParametersBase(Guid storageDomainId) {
        setStorageDomainId(storageDomainId);
    }

    public StorageDomainQueryParametersBase() {
        this(Guid.Empty);
    }
}
