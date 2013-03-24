package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class KeyValueModel extends EntityModel implements IModifyLines {

    public final static String SELECT_KEY = ConstantsManager.getInstance().getConstants().pleaseSelectKey();
    public final static String NO_KEYS = ConstantsManager.getInstance().getConstants().noKeyAvailable();

    ListModel keyValueLines;
    Map<String, String> allKeyValueMap;
    Map<String, List<String>> allRegExKeys;
    private Map<String, String> keyValueMap_used = new HashMap<String, String>();
    boolean disableEvent = false;
    private Object saveEntity;

    private final IEventListener keyChangedListener = new IEventListener() {

        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            if (disableEvent) {
                return;
            }
            ListModel listModel = (ListModel) sender;
            String key = null;
            if (listModel.getSelectedItem() != null) {
                key = (String) listModel.getSelectedItem();
            }
            boolean keySelected =
                    key != null &&
                    !key.equals(KeyValueModel.SELECT_KEY) &&
                    !key.equals(KeyValueModel.NO_KEYS);
            List<KeyValueLineModel> list =
                    new ArrayList<KeyValueLineModel>((List<KeyValueLineModel>) getKeyValueLines().getItems());
            for (KeyValueLineModel keyValueLineModel : list) {
                if (((String) keyValueLineModel.getKeys().getSelectedItem()).equals(key)) {
                    if (keySelected) {
                        if (allRegExKeys.containsKey(key)) {
                            keyValueLineModel.getValue().setIsAvailable(false);
                            keyValueLineModel.getValues().setIsAvailable(true);
                            keyValueLineModel.getValues().setItems(allRegExKeys.get(key));
                        } else {
                            keyValueLineModel.getValue().setIsAvailable(true);
                            keyValueLineModel.getValues().setIsAvailable(false);
                        }
                    } else {
                        keyValueLineModel.getValue().setIsAvailable(keySelected);
                        keyValueLineModel.getValues().setIsAvailable(keySelected);
                        keyValueLineModel.getValue().setEntity("");
                        keyValueLineModel.getValues().setSelectedItem(null);
                        keyValueLineModel.getValues().setItems(null);
                    }
                }
            }
            updateKeys();
        }
    };

    public ListModel getKeyValueLines() {
        return keyValueLines;
    }

    public void setKeyValueLines(ListModel keyValueLines) {
        this.keyValueLines = keyValueLines;
    }

    public KeyValueModel() {
        setKeyValueLines(new ListModel());
    }

    @Override
    public void setEntity(Object value) {
        if (allKeyValueMap == null) {
            saveEntity = value;
            return;
        }
        List<KeyValueLineModel> list = new ArrayList<KeyValueLineModel>();
        KeyValueLineModel lineModel;

        if (value != null) {
            String split = (String) value;
            if (split.isEmpty()) {
                return;
            }
            String[] lines = split.split(";"); //$NON-NLS-1$

            keyValueMap_used = new HashMap<String, String>();
            String[] splitLine;
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                splitLine = line.split("="); //$NON-NLS-1$
                String key = splitLine[0];
                if (allKeyValueMap.containsKey(key)) {
                    keyValueMap_used.put(key, splitLine[1]);
                }
            }

            for (String key : keyValueMap_used.keySet()) {
                lineModel = new KeyValueLineModel(this);
                lineModel.getKeys().setItems(getAvailbleKeys(key));
                lineModel.getKeys().setSelectedItem(key);
                if (allRegExKeys.containsKey(key)) {
                    lineModel.getValue().setIsAvailable(false);
                    lineModel.getValues().setIsAvailable(true);
                    lineModel.getValues().setItems(allRegExKeys.get(key));
                    lineModel.getValues().setSelectedItem(keyValueMap_used.get(key));
                } else {
                    lineModel.getValue().setEntity(keyValueMap_used.get(key));
                }
                list.add(lineModel);
            }
        } else {
            lineModel = new KeyValueLineModel(this);
            lineModel.getKeys().setItems(getAvailbleKeys());
            list.add(lineModel);
        }

        for (final KeyValueLineModel keyValueLineModel : list) {
            keyValueLineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        }
        getKeyValueLines().setItems(list);
    }

    public void setKeyValueString(List<String> lines) {
        if (lines == null) {
            return;
        }
        allKeyValueMap = new HashMap<String, String>();
        allRegExKeys = new HashMap<String, List<String>>();
        RegexValidation regexValidation = new RegexValidation();
        regexValidation.setExpression("\\^\\((([a-zA-Z0-9_]+[|]+)*)[a-zA-Z0-9_]+\\)\\$"); //$NON-NLS-1$
        String[] splitLine;
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            splitLine = line.split("="); //$NON-NLS-1$
            String key = splitLine[0];
            allKeyValueMap.put(key, splitLine[1]);
            ValidationResult valid = regexValidation.validate(allKeyValueMap.get(key));
            if (valid.getSuccess()) {
                String[] values = allKeyValueMap.get(key)
                        .substring(2, allKeyValueMap.get(key).length() - 2)
                        .split("\\|"); //$NON-NLS-1$
                allRegExKeys.put(splitLine[0], Arrays.asList(values));
            }
        }

        setEntity(saveEntity);
    }

    public List<String> getAvailbleKeys(String key) {
        List<String> list = getAvailbleKeys();
        if (!list.contains(key)) {
            list.add(0, key);
        }
        list.remove(SELECT_KEY);
        return list;
    }

    public List<String> getAvailbleKeys() {
        List<String> list = new ArrayList<String>(allKeyValueMap.keySet());
        for (String key : getUsedKeys()) {
            list.remove(key);
        }
        if (list.size() > 0) {
            list.add(0, SELECT_KEY);
        } else {
            list.add(NO_KEYS);
        }

        return list;
    }

    private List<String> getUsedKeys() {
        if (keyValueMap_used == null) {
            return new ArrayList<String>();
        } else {
            return new ArrayList<String>(keyValueMap_used.keySet());
        }
    }

    @Override
    public void addLine(KeyValueLineModel lineModel) {
        List<KeyValueLineModel> list =
                new ArrayList<KeyValueLineModel>((List<KeyValueLineModel>) getKeyValueLines().getItems());
        int counter = 0;
        for (KeyValueLineModel keyValueLineModel : list) {
            counter++;
            if (keyValueLineModel.equals(lineModel)) {
                break;
            }
        }
        KeyValueLineModel keyValueLineModel = new KeyValueLineModel(this);
        keyValueLineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        keyValueLineModel.getKeys().setItems(getAvailbleKeys());
        keyValueLineModel.getValue().setIsAvailable(false);
        list.add(counter, keyValueLineModel);
        getKeyValueLines().setItems(list);
    }

    @Override
    public void removeLine(KeyValueLineModel lineModel) {
        List<KeyValueLineModel> list =
                new ArrayList<KeyValueLineModel>((List<KeyValueLineModel>) getKeyValueLines().getItems());
        int counter = 0;
        for (KeyValueLineModel keyValueLineModel : list) {
            if (keyValueLineModel.equals(lineModel)) {
                break;
            }
            counter++;
        }
        list.remove(counter);
        getKeyValueLines().setItems(list);
        updateKeys();
        if (list.size() == 0) {
            addLine(new KeyValueLineModel(this));
        }
    }

    private void updateKeys() {
        if (getKeyValueLines().getItems() != null && keyValueMap_used != null) {
            disableEvent = true;
            keyValueMap_used.clear();
            for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getKeyValueLines().getItems()) {
                String key = (String) keyValueLineModel.getKeys()
                        .getSelectedItem();
                keyValueMap_used.put(key, "");
            }
            for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getKeyValueLines().getItems()) {
                String key = (String) keyValueLineModel.getKeys()
                        .getSelectedItem();
                keyValueLineModel.getKeys().setItems(getAvailbleKeys(key));
                if (!key.equals(NO_KEYS)) {
                    keyValueLineModel.getKeys().setSelectedItem(key);
                } else if (((List<KeyValueLineModel>) getKeyValueLines().getItems()).size() > 1) {
                    keyValueLineModel.getKeys().setSelectedItem(SELECT_KEY);
                }
            }
            disableEvent = false;
        }
    }

    @Override
    public String getEntity() {
        StringBuilder builder = new StringBuilder();
        if (getKeyValueLines().getItems() == null) {
            return "";
        }
        for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getKeyValueLines().getItems()) {
            String key = (String) keyValueLineModel.getKeys().getSelectedItem();
            if (key.equals(NO_KEYS) || key.equals(SELECT_KEY)) {
                continue;
            }
            builder.append(key);
            builder.append("="); //$NON-NLS-1$
            if (keyValueLineModel.getValue().getIsAvailable()) {
                builder.append(keyValueLineModel.getValue().getEntity());
            } else if (keyValueLineModel.getValues().getIsAvailable()) {
                builder.append(keyValueLineModel.getValues().getSelectedItem());
            }
            builder.append(";"); //$NON-NLS-1$
        }
        return builder.toString();
    }

    public boolean validate() {
        boolean isValid = true;
        if (getKeyValueLines().getItems() == null) {
            return isValid;
        }
        for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getKeyValueLines().getItems()) {
            String key = (String) keyValueLineModel.getKeys().getSelectedItem();
            if (key.equals(NO_KEYS) || key.equals(SELECT_KEY)) {
                continue;
            }

            keyValueLineModel.getValue().setIsValid(true);
            RegexValidation regexValidation = new RegexValidation();
            regexValidation.setMessage(ConstantsManager.getInstance()
                    .getMessages()
                    .customPropertyValueShouldBeInFormatReason(key, allKeyValueMap.get(key)));
            regexValidation.setExpression(allKeyValueMap.get(key));
            keyValueLineModel.getValue().ValidateEntity(
                    new IValidation[] { regexValidation });
            isValid &= keyValueLineModel.getValue().getIsValid();
        }
        return isValid;
    }
}
