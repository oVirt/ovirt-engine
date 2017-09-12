package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public abstract class BaseKeyModel<M extends KeyLineModel> extends ListModel<M> {
    private final String selectKey;
    private final String noKeys;
    boolean disableEvent = false;

    Set<String> allKeys = new HashSet<>();
    Set<String> usedKeys = new HashSet<>();

    public BaseKeyModel(String selectKey, String noKeys) {
        this.selectKey = selectKey;
        this.noKeys = noKeys;
    }

    protected void init(Set<String> allKeys, Set<String> usedKeys) {
        this.allKeys = new HashSet<>(allKeys);
        this.usedKeys = new HashSet<>(usedKeys);
        disableEvent = true;
        List<M> list = createLineModels(usedKeys);
        disableEvent = false;
        setItems(list);
    }

    protected abstract List<M> createLineModels(Set<String> usedKeys);

    protected abstract void initLineModel(M lineModel, String key);

    public final IEventListener<EventArgs> keyChangedListener = (ev, sender, args) -> {
        if (disableEvent) {
            return;
        }
        ListModel<String> listModel = (ListModel<String>) sender;
        String key = null;
        if (listModel.getSelectedItem() != null) {
            key = listModel.getSelectedItem();
        }
        for (M lineModel : getItems()) {
            if (lineModel.getKeys().getSelectedItem().equals(key)) {
                initLineModel(lineModel, key);
            }
        }
        updateKeys();
    };

    public M createNewLineModel() {
        return createNewLineModel(null);
    }

    public abstract M createNewLineModel(String key);

    public boolean isKeyValid(String key) {
        return !(key == null || key.equals(selectKey) || key.equals(noKeys));
    }

    protected List<String> getAvailableKeys(String key) {
        List<String> list = getAvailableKeys();
        boolean realKey = isKeyValid(key);
        if (realKey && !list.contains(key)) {
            list.add(0, key);
        }

        list.remove(selectKey);
        list.remove(noKeys);

        if (!realKey) {
            if (list.size() > 0) {
                list.add(0, selectKey);
            } else {
                list.add(noKeys);
            }
        }

        return list;
    }

    protected List<String> getAvailableKeys() {
        List<String> list =
                (allKeys == null) ? new LinkedList<>() : new LinkedList<>(allKeys);
        list.removeAll(getUsedKeys());
        if (list.size() > 0) {
            list.add(0, selectKey);
        } else {
            list.add(noKeys);
        }

        return list;
    }

    public List<String> getUsedKeys() {
        if (usedKeys == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(usedKeys);
        }
    }

    public void updateKeys() {
        if (getItems() != null && usedKeys != null) {
            disableEvent = true;
            usedKeys.clear();
            for (M lineModel : getItems()) {
                String key = lineModel.getKeys().getSelectedItem();
                usedKeys.add(key);
            }
            for (M lineModel : getItems()) {
                String key = lineModel.getKeys().getSelectedItem();
                lineModel.getKeys().setItems(getAvailableKeys(key));
                lineModel.getKeys().setSelectedItem(lineModel.getKeys().getItems().iterator().next());
            }
            disableEvent = false;
        }
    }
}
