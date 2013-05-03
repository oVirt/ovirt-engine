package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.common.businessentities.Identifiable;

public class EnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {
    private final HashMap<String, Integer> mEnumValues = new HashMap<String, Integer>();

    public <E extends Enum<E> & Identifiable> EnumValueAutoCompleter(Class<E> enumerationType) {

        for (E val : enumerationType.getEnumConstants()) {
            String ValName = val.name().toUpperCase();
            try {
                mEnumValues.put(ValName, val.getValue());
                mVerbs.add(ValName);
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
