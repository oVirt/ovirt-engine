package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

public class BitValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";
    private final Map<String, Integer> bitValues = new HashMap<>();

    public BitValueAutoCompleter() {
        bitValues.put(TRUE, 1);
        verbs.add(TRUE);
        bitValues.put(FALSE, 0);
        verbs.add(FALSE);
        buildCompletions();
    }

    public String convertFieldEnumValueToActualValue(String fieldValue) {
        String retval = "";
        if (bitValues.containsKey(fieldValue.toUpperCase())) {
            retval = bitValues.get(fieldValue.toUpperCase()).toString();
        }
        return retval;
    }
}
