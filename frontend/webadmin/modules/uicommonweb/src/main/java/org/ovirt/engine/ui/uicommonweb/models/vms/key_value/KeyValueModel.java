package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class KeyValueModel extends ListModel<KeyValueLineModel> {

    public final static String SELECT_KEY = ConstantsManager.getInstance().getConstants().pleaseSelectKey();
    public final static String NO_KEYS = ConstantsManager.getInstance().getConstants().noKeyAvailable();
    public final static String PROPERTIES_DELIMETER = ";"; //$NON-NLS-1$
    public final static String KEY_VALUE_DELIMETER = "="; //$NON-NLS-1$

    Map<String, String> allKeyValueMap;
    Map<String, List<String>> allRegExKeys;
    private Map<String, String> keyValueMap_used = new HashMap<String, String>();
    boolean disableEvent = false;
    private String saveEntity;

    public final IEventListener keyChangedListener = new IEventListener() {

        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            if (disableEvent) {
                return;
            }
            ListModel<String> listModel = (ListModel<String>) sender;
            String key = null;
            if (listModel.getSelectedItem() != null) {
                key = listModel.getSelectedItem();
            }
            for (KeyValueLineModel keyValueLineModel : getItems()) {
                if (keyValueLineModel.getKeys().getSelectedItem().equals(key)) {
                    initLineModel(keyValueLineModel, key);
                }
            }
            updateKeys();
        }
    };

    private void initLineModel(KeyValueLineModel keyValueLineModel, String key) {
        if (isKeyValid(key)) {
            boolean constrainedValue = allRegExKeys.containsKey(key);
            keyValueLineModel.getValue().setIsAvailable(!constrainedValue);
            keyValueLineModel.getValues().setIsAvailable(constrainedValue);
            if (constrainedValue) {
                keyValueLineModel.getValues().setItems(allRegExKeys.get(key));
            }
        } else {
            keyValueLineModel.getValue().setIsAvailable(false);
            keyValueLineModel.getValues().setIsAvailable(false);
            keyValueLineModel.getValue().setEntity("");
            keyValueLineModel.getValues().setSelectedItem(null);
            keyValueLineModel.getValues().setItems(null);
        }
    }

    private KeyValueLineModel createNewLineModel(String key) {
        KeyValueLineModel lineModel = new KeyValueLineModel();
        lineModel.getKeys().setItems(key == null ? getAvailableKeys() : getAvailableKeys(key));
        lineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        initLineModel(lineModel, key);
        return lineModel;
    }

    public KeyValueLineModel createNewLineModel() {
        return createNewLineModel(null);
    }

    public boolean isKeyValid(String key) {
        return !(key == null || key.equals(SELECT_KEY) || key.equals(NO_KEYS));
    }

    public void deserialize(String value) {
        if (allKeyValueMap == null) {
            saveEntity = value;
            return;
        }
        List<KeyValueLineModel> list = new ArrayList<KeyValueLineModel>();
        KeyValueLineModel lineModel;

        if (value != null) {
            if (value.isEmpty()) {
                return;
            }
            String[] lines = value.split(PROPERTIES_DELIMETER);

            keyValueMap_used = new HashMap<String, String>();
            String[] splitLine;
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                splitLine = line.split(KEY_VALUE_DELIMETER, 2);
                String key = splitLine[0];
                if (allKeyValueMap.containsKey(key)) {
                    keyValueMap_used.put(key, splitLine[1]);
                }
            }

            disableEvent = true;
            for (Map.Entry<String, String> entry : keyValueMap_used.entrySet()) {
                lineModel = createNewLineModel(entry.getKey());
                lineModel.getKeys().setSelectedItem(entry.getKey());
                if (allRegExKeys.containsKey(entry.getKey())) {
                    lineModel.getValues().setSelectedItem(entry.getValue());
                } else {
                    lineModel.getValue().setEntity(entry.getValue());
                }
                list.add(lineModel);
            }
            disableEvent = false;
        }
        setItems(list);
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
            splitLine = line.split(KEY_VALUE_DELIMETER, 2);
            String key = splitLine[0];
            allKeyValueMap.put(key, splitLine[1]);
            ValidationResult valid = regexValidation.validate(allKeyValueMap.get(key));
            if (valid.getSuccess()) {
                String[] values = allKeyValueMap.get(key)
                        .substring(2, allKeyValueMap.get(key).length() - 2)
                        .split("\\|"); //$NON-NLS-1$
                allRegExKeys.put(key, Arrays.asList(values));
            }
        }

        deserialize(saveEntity);
    }

    public List<String> getAvailableKeys(String key) {
        List<String> list = getAvailableKeys();
        boolean realKey = isKeyValid(key);
        if (realKey && !list.contains(key)) {
            list.add(0, key);
        }

        list.remove(SELECT_KEY);
        list.remove(NO_KEYS);

        if (!realKey) {
            if (list.size() > 0) {
                list.add(0, SELECT_KEY);
            } else {
                list.add(NO_KEYS);
            }
        }

        return list;
    }

    public List<String> getAvailableKeys() {
        List<String> list =
                (allKeyValueMap == null) ? new LinkedList<String>() : new LinkedList<String>(allKeyValueMap.keySet());
        list.removeAll(getUsedKeys());
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

    public int possibleKeysCount() {
        return allKeyValueMap == null ? 0 : allKeyValueMap.size();
    }

    public void updateKeys() {
        if (getItems() != null && keyValueMap_used != null) {
            disableEvent = true;
            keyValueMap_used.clear();
            for (KeyValueLineModel keyValueLineModel : getItems()) {
                String key = (String) keyValueLineModel.getKeys().getSelectedItem();
                keyValueMap_used.put(key, "");
            }
            for (KeyValueLineModel keyValueLineModel : getItems()) {
                String key = (String) keyValueLineModel.getKeys().getSelectedItem();
                keyValueLineModel.getKeys().setItems(getAvailableKeys(key));
                keyValueLineModel.getKeys().setSelectedItem(keyValueLineModel.getKeys().getItems().iterator().next());
            }
            disableEvent = false;
        }
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        if (getItems() == null) {
            return "";
        }
        for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getItems()) {
            String key = (String) keyValueLineModel.getKeys().getSelectedItem();
            if (!isKeyValid(key)) {
                continue;
            }
            builder.append(key);
            builder.append(KEY_VALUE_DELIMETER);
            if (keyValueLineModel.getValue().getIsAvailable()) {
                builder.append(keyValueLineModel.getValue().getEntity());
            } else if (keyValueLineModel.getValues().getIsAvailable()) {
                builder.append(keyValueLineModel.getValues().getSelectedItem());
            }
            builder.append(PROPERTIES_DELIMETER);
        }
        return builder.toString();
    }

    public boolean validate() {
        boolean isValid = true;
        if (getItems() == null) {
            return isValid;
        }
        for (KeyValueLineModel keyValueLineModel : (List<KeyValueLineModel>) getItems()) {
            String key = (String) keyValueLineModel.getKeys().getSelectedItem();
            if (!isKeyValid(key)) {
                continue;
            }

            keyValueLineModel.getValue().setIsValid(true);
            RegexValidation regexValidation = new RegexValidation();
            regexValidation.setMessage(ConstantsManager.getInstance()
                    .getMessages()
                    .customPropertyValueShouldBeInFormatReason(key, allKeyValueMap.get(key)));
            regexValidation.setExpression(allKeyValueMap.get(key));
            keyValueLineModel.getValue().validateEntity(
                    new IValidation[] { regexValidation });
            isValid &= keyValueLineModel.getValue().getIsValid();
        }
        return isValid;
    }

    /**
     * Converts properties from string to map. Method assumes, that properties are syntactically valid
     *
     * @param properties
     *            specified properties
     * @return map containing all properties ({@code LinkedHashMap} is used to ensure properties order is
     *         constant)
     */
    public static Map<String, String> convertProperties(String properties) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (!StringHelper.isNullOrEmpty(properties)) {
            String keyValuePairs[] = properties.split(PROPERTIES_DELIMETER);
            for (String keyValuePairStr : keyValuePairs) {
                String[] pairParts = keyValuePairStr.split(KEY_VALUE_DELIMETER, 2);
                String key = pairParts[0];
                // property value may be null
                String value = pairParts[1];
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Converts properties from map to string.
     *
     * @param properties
     *            specified properties
     * @return string containing all properties in map
     */
    public static String convertProperties(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                sb.append(e.getKey());
                sb.append(KEY_VALUE_DELIMETER);
                sb.append(e.getValue());
                sb.append(PROPERTIES_DELIMETER);
            }
            // remove last PROPERTIES_DELIMETER
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
