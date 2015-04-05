package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetCinderEntityByStorageDomainIdParameters extends IdQueryParameters {

    private Guid entityId;

    public GetCinderEntityByStorageDomainIdParameters() {
    }

    public GetCinderEntityByStorageDomainIdParameters(Guid entityId, Guid storageDomainId) {
        super(storageDomainId);
        this.entityId = entityId;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid value) {
        entityId = value;
    }
}
