package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class KeyModel extends BaseKeyModel<KeyLineModel> {

    public KeyModel(String selectKey, String noKeys) {
        super(selectKey, noKeys);
    }

    @Override
    public KeyLineModel createNewLineModel(String key) {
        KeyLineModel lineModel = new KeyLineModel();
        lineModel.getKeys().setItems(key == null ? getAvailableKeys() : getAvailableKeys(key));
        lineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        initLineModel(lineModel, key);
        return lineModel;
    }

    @Override
    protected List<KeyLineModel> createLineModels(Set<String> usedKeys) {
        List<KeyLineModel> lineModels = new ArrayList<>();

        for (String key : usedKeys) {
            KeyLineModel lineModel = createNewLineModel(key);
            lineModel.getKeys().setSelectedItem(key);
            lineModels.add(lineModel);
        }

        return lineModels;
    }
}
