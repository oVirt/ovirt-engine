package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class KeyValueModel extends BaseKeyModel {

    public static final String PROPERTIES_DELIMETER = ";"; //$NON-NLS-1$
    public static final String KEY_VALUE_DELIMETER = "="; //$NON-NLS-1$
    private String saveEntity;

    public KeyValueModel() {
        super(ConstantsManager.getInstance().getConstants().pleaseSelectKey(), ConstantsManager.getInstance().getConstants().noKeyAvailable());
    }
    Map<String, String> allKeyValueMap;
    Map<String, List<String>> allRegExKeys;
    private Map<String, String> keyValueMap_used = new HashMap<>();

    @Override
    protected void initLineModel(KeyValueLineModel keyValueLineModel, String key) {
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

    @Override
    protected void setValueByKey(KeyValueLineModel lineModel, String key) {
        if (allRegExKeys.containsKey(key)) {
            lineModel.getValues().setSelectedItem(keyValueMap_used.get(key));
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
                }

            }
        }
        init(allKeyValueMap.keySet(), keyValueMap_used.keySet());
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
