package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

public class BitValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";
    private final HashMap<String, Integer> mBitValues = new HashMap<String, Integer>();

    public BitValueAutoCompleter() {
        mBitValues.put(TRUE, 1);
        mVerbs.add(TRUE);
        mBitValues.put(FALSE, 0);
        mVerbs.add(FALSE);
        buildCompletions();
    }

    public String convertFieldEnumValueToActualValue(String fieldValue) {
        String retval = "";
        if (mBitValues.containsKey(fieldValue.toUpperCase())) {
            retval = mBitValues.get(fieldValue.toUpperCase()).toString();
        }
        return retval;
    }
}
