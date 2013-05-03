package org.ovirt.engine.core.searchbackend;

public class BitValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private final java.util.HashMap<String, Integer> mBitValues = new java.util.HashMap<String, Integer>();

    public BitValueAutoCompleter() {
        mBitValues.put("TRUE", 1);
        mVerbs.add("TRUE");
        mBitValues.put("FALSE", 0);
        mVerbs.add("FALSE");
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
