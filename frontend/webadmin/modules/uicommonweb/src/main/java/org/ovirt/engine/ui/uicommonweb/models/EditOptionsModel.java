package org.ovirt.engine.ui.uicommonweb.models;

public class EditOptionsModel extends Model {

    private EntityModel<Boolean> enableConnectAutomatically;

    public EntityModel<Boolean> getEnableConnectAutomatically() {
        return enableConnectAutomatically;
    }

    public void setEnableConnectAutomatically(EntityModel<Boolean> value) {
        this.enableConnectAutomatically = value;
    }

    private EntityModel<String> publicKey;

    public EntityModel<String> getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(EntityModel<String> textInput) {
        this.publicKey = textInput;
    }

    public EditOptionsModel() {
        setEnableConnectAutomatically(new EntityModel<>(true));
        setPublicKey(new EntityModel<String>());
    }

}
