package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class SanStorageModelProvider<M extends EntityModel> implements ModelProvider<M> {

    M model;

    public SanStorageModelProvider(M model) {
        this.model = model;
    }

    @Override
    public M getModel() {
        return model;
    }

    @Override
    public void setEntity(Object value) {
    }

}
