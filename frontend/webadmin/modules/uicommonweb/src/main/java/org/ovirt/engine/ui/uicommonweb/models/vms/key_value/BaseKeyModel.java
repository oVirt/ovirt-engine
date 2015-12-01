package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public abstract class BaseKeyModel extends ListModel<KeyValueLineModel> {
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
        List<KeyValueLineModel> list = new ArrayList<>();
        disableEvent = true;
        for (String key : usedKeys) {
            KeyValueLineModel lineModel = createNewLineModel(key);
            lineModel.getKeys().setSelectedItem(key);
            setValueByKey(lineModel, key);
            list.add(lineModel);
        }
        disableEvent = false;
        setItems(list);
    }

    protected abstract void initLineModel(KeyValueLineModel lineModel, String key);

    protected abstract void setValueByKey(KeyValueLineModel lineModel, String key);

    public final IEventListener<EventArgs> keyChangedListener = new IEventListener<EventArgs>() {

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (disableEvent) {
                return;
            }
            ListModel<String> listModel = (ListModel<String>) sender;
            String key = null;
            if (listModel.getSelectedItem() != null) {
                key = listModel.getSelectedItem();
            }
            for (KeyValueLineModel lineModel : getItems()) {
                if (lineModel.getKeys().getSelectedItem().equals(key)) {
                    initLineModel(lineModel, key);
                }
            }
            updateKeys();
        }
    };

    public KeyValueLineModel createNewLineModel() {
        return createNewLineModel(null);
    }

    public KeyValueLineModel createNewLineModel(String key) {
        KeyValueLineModel lineModel = new KeyValueLineModel();
        lineModel.getKeys().setItems(key == null ? getAvailableKeys() : getAvailableKeys(key));
        lineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        initLineModel(lineModel, key);
        return lineModel;
    }

    public boolean isKeyValid(String key) {
        return !(key == null || key.equals(selectKey) || key.equals(noKeys));
    }

    private List<String> getAvailableKeys(String key) {
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

    private List<String> getAvailableKeys() {
        List<String> list =
                (allKeys == null) ? new LinkedList<String>() : new LinkedList<>(allKeys);
        list.removeAll(getUsedKeys());
        if (list.size() > 0) {
            list.add(0, selectKey);
        } else {
            list.add(noKeys);
        }

        return list;
    }

    private List<String> getUsedKeys() {
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
            for (KeyValueLineModel lineModel : getItems()) {
                String key = lineModel.getKeys().getSelectedItem();
                usedKeys.add(key);
            }
            for (KeyValueLineModel lineModel : getItems()) {
                String key = lineModel.getKeys().getSelectedItem();
                lineModel.getKeys().setItems(getAvailableKeys(key));
                lineModel.getKeys().setSelectedItem(lineModel.getKeys().getItems().iterator().next());
            }
            disableEvent = false;
        }
    }
}
