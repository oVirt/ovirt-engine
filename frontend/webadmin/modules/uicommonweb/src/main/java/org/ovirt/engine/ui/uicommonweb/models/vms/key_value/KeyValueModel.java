package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class KeyValueModel extends BaseKeyModel<KeyValueLineModel> {

    public static final String PROPERTIES_DELIMETER = ";"; //$NON-NLS-1$
    public static final String KEY_VALUE_DELIMETER = "="; //$NON-NLS-1$
    private String saveEntity;
    private boolean showInvalidKeys;

    public KeyValueModel() {
        super(ConstantsManager.getInstance().getConstants().pleaseSelectKey(), ConstantsManager.getInstance().getConstants().noKeyAvailable());
    }

    Map<String, String> allKeyValueMap;
    Map<String, List<String>> allRegExKeys;
    private Map<String, String> keyValueMap_used = new HashMap<>();
    private Map<String, String> keyValueMap_invalidKey = new HashMap<>();
    private boolean useEditableKey;
    //Available only if using editable key
    private boolean maskValueField;

    @Override
    protected void initLineModel(KeyValueLineModel keyValueLineModel, String key) {
        if (useEditableKey) {
            keyValueLineModel.getValues().setIsAvailable(false);
            keyValueLineModel.getKeys().setIsAvailable(false);
            keyValueLineModel.getEditableKey().setIsAvailable(true);
            keyValueLineModel.getPasswordValueField().setIsAvailable(isMaskValueField());
            keyValueLineModel.getPasswordValueField().setIsChangeable(isMaskValueField());
            keyValueLineModel.getValue().setIsAvailable(!isMaskValueField());
            keyValueLineModel.getValue().setIsChangeable(!isMaskValueField());
            keyValueLineModel.getPasswordValueField().setEntity("");
            keyValueLineModel.getValue().setEntity("");
            keyValueLineModel.getEditableKey().setEntity("");
        } else if (isKeyValid(key)) {
            boolean constrainedValue = allRegExKeys.containsKey(key);
            keyValueLineModel.getValue().setIsAvailable(!constrainedValue);
            keyValueLineModel.getValues().setIsAvailable(constrainedValue);
            keyValueLineModel.getEditableKey().setIsAvailable(false);
            keyValueLineModel.getEditableKey().setEntity("");
            keyValueLineModel.getPasswordValueField().setIsAvailable(false);
            keyValueLineModel.getPasswordValueField().setEntity("");
            if (constrainedValue) {
                keyValueLineModel.getValues().setItems(allRegExKeys.get(key));
            }
        } else if (showInvalidKeys){
            keyValueLineModel.getValue().setIsAvailable(true);
            keyValueLineModel.getValue().setEntity("");
            keyValueLineModel.getEditableKey().setIsAvailable(false);
            keyValueLineModel.getEditableKey().setEntity("");
            keyValueLineModel.getPasswordValueField().setIsAvailable(false);
            keyValueLineModel.getPasswordValueField().setEntity("");
            keyValueLineModel.getValues().setIsAvailable(false);
            keyValueLineModel.getValues().setSelectedItem(null);
            keyValueLineModel.getValues().setItems(null);
        } else {
            keyValueLineModel.getValue().setIsAvailable(false);
            keyValueLineModel.getValue().setEntity("");
            keyValueLineModel.getEditableKey().setIsAvailable(false);
            keyValueLineModel.getEditableKey().setEntity("");
            keyValueLineModel.getPasswordValueField().setIsAvailable(false);
            keyValueLineModel.getPasswordValueField().setEntity("");
            keyValueLineModel.getValues().setIsAvailable(false);
            keyValueLineModel.getValues().setSelectedItem(null);
            keyValueLineModel.getValues().setItems(null);
        }
    }

    public void useEditableKey(boolean useEditableKey) {
        this.useEditableKey = useEditableKey;
    }

    public boolean isEditableKey() {
        return this.useEditableKey;
    }

    public boolean isMaskValueField() {
        return maskValueField;
    }

    public void setMaskValueField(boolean maskValueField) {
        this.maskValueField = maskValueField && isEditableKey();
    }

    protected void setValueByKey(KeyValueLineModel lineModel, String key) {
        if (allRegExKeys.containsKey(key)) {
            lineModel.getValues().setSelectedItem(keyValueMap_used.get(key));
        } else if (showInvalidKeys && keyValueMap_invalidKey.containsKey(key)) {
            lineModel.getValue().setEntity(keyValueMap_invalidKey.get(key));
        } else {
            lineModel.getValue().setEntity(keyValueMap_used.get(key));
        }
    }

    public void deserialize(String value) {
        if (allKeyValueMap == null) {
            saveEntity = value;
            return;
        }

        //always reset the list of items when the item changes
        keyValueMap_used = new HashMap<>();
        keyValueMap_invalidKey = new HashMap<>();
        if (value != null && !value.isEmpty()) {
            String[] lines = value.split(PROPERTIES_DELIMETER);
            String[] splitLine;
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                splitLine = line.split(KEY_VALUE_DELIMETER, 2);
                String key = splitLine[0];
                if (allKeyValueMap.containsKey(key)) {
                    keyValueMap_used.put(key, splitLine[1]);
                } else if (showInvalidKeys) {
                    keyValueMap_invalidKey.put(key, splitLine[1]);
                }

            }
        }
        Set<String> keysToShow = new HashSet<>(keyValueMap_used.keySet());
        if (showInvalidKeys) {
            keysToShow.addAll(keyValueMap_invalidKey.keySet());
        }
        init(allKeyValueMap.keySet(), keysToShow);
    }

    public void setKeyValueString(List<String> lines) {
        if (lines == null) {
            return;
        }
        allKeyValueMap = new HashMap<>();
        allRegExKeys = new HashMap<>();
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

    public void setKeyValueMap(Map<String, String> keyValueMap) {
        if (keyValueMap == null) {
            return;
        }

        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
            lines.add(entry.getKey() + '=' + entry.getValue());
        }
        setKeyValueString(lines);
    }

    public Map<String, Object> serializeToMap() {
        Map<String, Object> keyValueMap = new HashMap<>();

        if (getItems() == null) {
            return null;
        }

        for (KeyValueLineModel keyValueLineModel : getItems()) {
            if (keyValueLineModel.getEditableKey().getEntity() != null && !keyValueLineModel.getEditableKey().getEntity().equals("")) {
                if (keyValueLineModel.getPasswordValueField().getEntity() != null && !keyValueLineModel.getPasswordValueField().getEntity().equals("")) {
                    keyValueMap.put(keyValueLineModel.getEditableKey().getEntity(),
                            keyValueLineModel.getPasswordValueField().getEntity());
                } else {
                    keyValueMap.put(keyValueLineModel.getEditableKey().getEntity(),
                            keyValueLineModel.getValue().getEntity());
                }

            }
        }

        return keyValueMap;
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        if (getItems() == null) {
            return "";
        }
        for (KeyValueLineModel keyValueLineModel : getItems()) {
            String key = keyValueLineModel.getKeys().getSelectedItem();
            if (!isKeyValid(key)) {
                continue;
            }
            builder.append(key);
            builder.append(KEY_VALUE_DELIMETER);
            if (keyValueLineModel.getValue().getIsAvailable()) {
                builder.append(keyValueLineModel.getValue().getEntity());
            } else if (keyValueLineModel.getValues().getIsAvailable()) {
                builder.append(keyValueLineModel.getValues().getSelectedItem());
            } else if (keyValueLineModel.getEditableKey().getIsAvailable()) {
                builder.append(keyValueLineModel.getEditableKey().getEntity());
            }
            builder.append(PROPERTIES_DELIMETER);
        }
        // remove the last delimiter
        if (builder.toString().endsWith(PROPERTIES_DELIMETER)) {
            return builder.subSequence(0, builder.length() - PROPERTIES_DELIMETER.length()).toString();
        }
        return builder.toString();
    }

    public boolean validate() {
        setIsValid(true);
        if (getItems() == null || !getIsAvailable()) {
            return true;
        }
        boolean isValid = true;
        for (KeyValueLineModel keyValueLineModel : getItems()) {
            String key = keyValueLineModel.getKeys().getSelectedItem();

            if (showInvalidKeys) {
                if (keyValueMap_invalidKey.containsKey(key)) {
                    keyValueLineModel.getKeys()
                            .setIsValid(false, ConstantsManager.getInstance().getConstants().invalidKey());
                    isValid = false;
                    continue;
                }
            }

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
        setIsValid(isValid);
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
        Map<String, String> map = new LinkedHashMap<>();
        if (!StringHelper.isNullOrEmpty(properties)) {
            String[] keyValuePairs = properties.split(PROPERTIES_DELIMETER);
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
     * @param map
     *            map of properties
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

    @Override
    public KeyValueLineModel createNewLineModel(String key) {
        KeyValueLineModel lineModel = new KeyValueLineModel();
        if (!useEditableKey) {
            lineModel.getKeys().setItems(key == null ? getAvailableKeys() : getAvailableKeys(key));
            lineModel.getKeys().getSelectedItemChangedEvent().addListener(keyChangedListener);
        }
        initLineModel(lineModel, key);
        return lineModel;
    }

    public void createLineModelsFromMap(Map<String, Object> keyValueMap) {
        List<KeyValueLineModel> lineModels = new ArrayList<>();
        if (useEditableKey) {
            for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
                KeyValueLineModel lineModel = createNewLineModel(null);
                lineModel.getEditableKey().setEntity(entry.getKey());
                if (isMaskValueField()) {
                    lineModel.getPasswordValueField().setEntity((String) entry.getValue());
                } else {
                    lineModel.getValue().setEntity((String) entry.getValue());
                }
                lineModels.add(lineModel);
            }
        }
        setItems(lineModels);
    }

    @Override
    protected List<KeyValueLineModel> createLineModels(Set<String> usedKeys) {
        List<KeyValueLineModel> lineModels = new ArrayList<>();

        if (useEditableKey) {
            KeyValueLineModel lineModel = createNewLineModel(null);
            lineModels.add(lineModel);
        } else {
            for (String key : usedKeys) {
                KeyValueLineModel lineModel = createNewLineModel(key);
                lineModel.getKeys().setSelectedItem(key);
                setValueByKey(lineModel, key);
                lineModels.add(lineModel);
            }
        }

        return lineModels;
    }

    public void setShowInvalidKeys(boolean showInvalidKeys) {
        this.showInvalidKeys = showInvalidKeys;
    }
}
