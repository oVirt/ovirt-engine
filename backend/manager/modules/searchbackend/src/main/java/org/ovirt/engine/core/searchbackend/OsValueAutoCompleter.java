package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OsValueAutoCompleter implements IConditionValueAutoCompleter {

    private static String[] EMPTY_COMPLETION_LIST = new String[]{};
    private Map<Integer, String> map;

    public OsValueAutoCompleter(Map<Integer, String> vmCompletionMap) {
        this.map = vmCompletionMap;
    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            if (fieldValue.startsWith(e.getValue())) {
                return e.getKey().toString();
            }
        }
        return "";
    }

    @Override
    public String[] getCompletion(String wordPart) {
        if (wordPart == null || wordPart.isEmpty()) {
            return EMPTY_COMPLETION_LIST;
        }
        List<String> list = new ArrayList<String>();
        for (String osName : map.values()) {
            if (osName.startsWith(wordPart)) {
                list.add(osName);
            }
        }
        return list.toArray(new String[]{});
    }

    @Override
    public boolean validate(String text) {
        return true;
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
