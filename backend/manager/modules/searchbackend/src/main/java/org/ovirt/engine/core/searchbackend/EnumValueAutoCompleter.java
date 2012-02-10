package org.ovirt.engine.core.searchbackend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.compat.EnumCompat;

public class EnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private final java.util.HashMap<String, Integer> mEnumValues = new java.util.HashMap<String, Integer>();

    public EnumValueAutoCompleter(java.lang.Class enumerationType) {

        for (int val : EnumCompat.GetIntValues(enumerationType)) {
            String ValName = EnumCompat.GetName(enumerationType, val).toUpperCase();
            try {
                mEnumValues.put(ValName, val);
                mVerbs.put(ValName, ValName);
            } catch (RuntimeException e) {
                log.error("EnumValueAutoCompleter. Failed to add " + ValName + " .Exception : " + e.getMessage(), e);
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

    private static Log log = LogFactory.getLog(EnumValueAutoCompleter.class);
}
