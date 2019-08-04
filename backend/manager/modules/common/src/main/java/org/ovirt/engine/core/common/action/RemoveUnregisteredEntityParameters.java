package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class RemoveUnregisteredEntityParameters extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -149288206548640151L;

    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid entityId;

    public RemoveUnregisteredEntityParameters() {
        this(Guid.Empty, Guid.Empty, Guid.Empty);
    }

    public RemoveUnregisteredEntityParameters(Guid entityId, Guid storageDomainId, Guid storagePoolId) {
        this.setEntityId(entityId);
        this.setStorageDomainId(storageDomainId);
        this.setStoragePoolId(storagePoolId);
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }
}
