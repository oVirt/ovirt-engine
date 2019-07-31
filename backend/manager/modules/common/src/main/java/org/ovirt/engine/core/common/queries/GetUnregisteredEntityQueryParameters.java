package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredEntityQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = 5636187447464159139L;

    private Guid storageDomainId;
    private Guid entityId;

    public GetUnregisteredEntityQueryParameters() {
    }

    public GetUnregisteredEntityQueryParameters(Guid storageDomainId, Guid entityId) {
        this.storageDomainId = storageDomainId;
        this.entityId = entityId;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
