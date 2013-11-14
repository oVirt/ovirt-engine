package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class KeyValueLineModel extends Model {

    ListModel<String> keys;
    ListModel<String> values;
    EntityModel<String> value;

    public ListModel<String> getKeys() {
        return keys;
    }

    public void setKeys(ListModel<String> keys) {
        this.keys = keys;
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

    public KeyValueLineModel() {
        setKeys(new ListModel<String>());
        setValue(new EntityModel<String>());
        setValues(new ListModel<String>());
        getValue().setIsAvailable(false);
        getValues().setIsAvailable(false);
    }

}
