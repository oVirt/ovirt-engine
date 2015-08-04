package org.ovirt.engine.ui.uicommonweb.models;

public class EditOptionsModel extends Model {

    private EntityModel<Boolean> enableConnectAutomatically;

    public EntityModel<Boolean> getEnableConnectAutomatically() {
        return enableConnectAutomatically;
    }

    public void setEnableConnectAutomatically(EntityModel<Boolean> value) {
        this.enableConnectAutomatically = value;
    }

    public EditOptionsModel() {
        setEnableConnectAutomatically(new EntityModel<Boolean>(true));
    }

}
