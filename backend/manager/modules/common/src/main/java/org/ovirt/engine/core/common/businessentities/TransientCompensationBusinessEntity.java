package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public abstract class TransientCompensationBusinessEntity implements BusinessEntity<Serializable> {

    private Serializable id;
    private TransientEntityType transientEntityType;

    //hide me. No-arg constructor should not be needed for deserialization. Preset only to please static analysis.
    private TransientCompensationBusinessEntity() { }

    public TransientCompensationBusinessEntity(TransientEntityType transientEntityType) {
        this(null, transientEntityType);
    }

    public TransientCompensationBusinessEntity(Serializable id,
            TransientEntityType transientEntityType) {
        this.id = id;
        this.transientEntityType = transientEntityType;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        this.id = id;
    }

    public TransientEntityType getTransientEntityType() {
        return transientEntityType;
    }
}
