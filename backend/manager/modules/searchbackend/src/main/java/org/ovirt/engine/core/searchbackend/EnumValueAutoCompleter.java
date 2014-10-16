package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {

    private static final Logger log = LoggerFactory.getLogger(EnumValueAutoCompleter.class);

    private final HashMap<String, Integer> mEnumValues = new HashMap<String, Integer>();

    public <E extends Enum<E> & Identifiable> EnumValueAutoCompleter(Class<E> enumerationType) {

        for (E val : enumerationType.getEnumConstants()) {
            String ValName = val.name().toUpperCase();
            try {
                mEnumValues.put(ValName, val.getValue());
                mVerbs.add(ValName);
            } catch (RuntimeException e) {
                log.error("EnumValueAutoCompleter. Failed to add '{}': {}", ValName, e.getMessage());
                log.debug("Exception", e);
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
}
