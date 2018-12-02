package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class KeyValueLineModel extends KeyLineModel {

    ListModel<String> values;
    EntityModel<String> value;
    EntityModel<String> editableKey;
    EntityModel<String> passwordValueField;


    public KeyValueLineModel() {
        super();
        setValue(new EntityModel<String>());
        setValues(new ListModel<String>());
        setEditableKey(new EntityModel<String>());
        setPasswordValueField(new EntityModel<String>());

        getValue().setIsAvailable(false);
        getValues().setIsAvailable(false);
        getEditableKey().setIsAvailable(false);
        getPasswordValueField().setIsAvailable(false);
    }

    public ListModel<String> getValues() {
        return values;
    }

    public void setValues(ListModel<String> values) {
        this.values = values;
    }

    public EntityModel<String> getValue() {
        return value;
    }

    public void setValue(EntityModel<String> value) {
        this.value = value;
    }

    public EntityModel<String> getEditableKey() {
        return editableKey;
    }

    public void setEditableKey(EntityModel<String> editableKey) {
        this.editableKey = editableKey;
    }

    public EntityModel<String> getPasswordValueField() {
        return passwordValueField;
    }

    public void setPasswordValueField(EntityModel<String> passwordValueField) {
        this.passwordValueField = passwordValueField;
    }

    @Override
    public void cleanup() {
        getValue().cleanup();
        getValues().cleanup();
        super.cleanup();
    }
}
