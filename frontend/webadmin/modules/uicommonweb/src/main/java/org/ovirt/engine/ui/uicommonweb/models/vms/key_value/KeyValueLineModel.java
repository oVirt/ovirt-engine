package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class KeyValueLineModel extends KeyLineModel {

    ListModel<String> values;
    EntityModel<String> value;

    public KeyValueLineModel() {
        super();
        setValue(new EntityModel<String>());
        setValues(new ListModel<String>());
        getValue().setIsAvailable(false);
        getValues().setIsAvailable(false);
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

    @Override
    public void cleanup() {
        getValue().cleanup();
        getValues().cleanup();
        super.cleanup();
    }
}
