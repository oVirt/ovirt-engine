package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class KeyLineModel extends Model {

    ListModel<String> keys;

    public ListModel<String> getKeys() {
        return keys;
    }

    public void setKeys(ListModel<String> keys) {
        this.keys = keys;
    }

    public KeyLineModel() {
        setKeys(new ListModel<String>());
    }

    @Override
    public void cleanup() {
        getKeys().cleanup();
        super.cleanup();
    }
}
