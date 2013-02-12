package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public abstract class ImportEntityData {
    protected Object entity;
    boolean isExistsInSystem;
    private EntityModel clone;

    public ImportEntityData() {
        setClone(new EntityModel(false));
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public boolean isExistsInSystem() {
        return isExistsInSystem;
    }

    public void setExistsInSystem(boolean isExistsInSystem) {
        this.isExistsInSystem = isExistsInSystem;
    }

    public EntityModel getClone() {
        return clone;
    }

    public void setClone(EntityModel clone) {
        this.clone = clone;
    }
}
