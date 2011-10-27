package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;

public class EnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private final java.util.HashMap<String, Integer> mEnumValues = new java.util.HashMap<String, Integer>();

    public EnumValueAutoCompleter(java.lang.Class enumerationType) {

        for (int val : EnumCompat.GetIntValues(enumerationType)) {
            String ValName = EnumCompat.GetName(enumerationType, val).toUpperCase();
            try {
                mEnumValues.put(ValName, val);
                mVerbs.put(ValName, ValName);
            } catch (RuntimeException e) {
                log.errorFormat("EnumValueAutoCompleter. Failed to add {0}.Exception :{1} ", ValName, e);
            }

        }
        buildCompletions();
    }

    public String convertFieldEnumValueToActualValue(String fieldValue) {
        String retval = "";
        if (mEnumValues.containsKey(fieldValue.toUpperCase())) {
            retval = mEnumValues.get(fieldValue.toUpperCase()).toString();
        }
        return retval;
    }

    private static LogCompat log = LogFactoryCompat.getLog(EnumValueAutoCompleter.class);
}
