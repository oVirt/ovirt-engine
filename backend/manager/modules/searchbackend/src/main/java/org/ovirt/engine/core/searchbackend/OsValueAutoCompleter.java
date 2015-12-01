package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OsValueAutoCompleter implements IConditionValueAutoCompleter {

    private Map<Integer, String> map;

    public OsValueAutoCompleter(Map<Integer, String> vmCompletionMap) {
        this.map = vmCompletionMap;
    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            if (fieldValue.equalsIgnoreCase(e.getValue())) {
                return e.getKey().toString();
            }
        }
        return "";
    }

    @Override
    public String[] getCompletion(String wordPart) {
        if (wordPart == null || wordPart.isEmpty()) {
            return map.values().toArray(new String[]{});
        }
        List<String> list = new ArrayList<>();
        for (String osName : map.values()) {
            if (osName.contains(wordPart)) {
                list.add(osName);
            }
        }
        return list.toArray(new String[]{});
    }

    @Override
    public boolean validate(String text) {
        text = text.trim();
        for (String os : map.values()) {
            if (os.equals(text)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateCompletion(String text) {
        return true;
    }

    @Override
    public String changeCaseDisplay(String text) {
        return text;
    }

}
