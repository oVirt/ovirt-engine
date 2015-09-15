package org.ovirt.engine.ui.uicommonweb.models;

public class EditOptionsModel extends Model {

    private EntityModel<String> publicKey;

    public EntityModel<String> getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(EntityModel<String> textInput) {
        this.publicKey = textInput;
    }

    public EditOptionsModel() {
        setPublicKey(new EntityModel<String>());
    }

}
