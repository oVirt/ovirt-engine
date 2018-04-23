package org.ovirt.engine.core.searchbackend;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumValueAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {

    private static final Logger log = LoggerFactory.getLogger(EnumValueAutoCompleter.class);

    private final Map<String, Integer> enumValues = new HashMap<>();

    public <E extends Enum<E> & Identifiable> EnumValueAutoCompleter(Class<E> enumerationType) {

        for (E val : enumerationType.getEnumConstants()) {
            String ValName = val.name().toUpperCase();
            try {
                enumValues.put(ValName, val.getValue());
                verbs.add(ValName);
            } catch (RuntimeException e) {
                log.error("EnumValueAutoCompleter. Failed to add '{}': {}", ValName, e.getMessage());
                log.debug("Exception", e);
            }

        }
        buildCompletions();
    }

    public String convertFieldEnumValueToActualValue(String fieldValue) {
        String retval = "";
        if (enumValues.containsKey(fieldValue.toUpperCase())) {
            retval = enumValues.get(fieldValue.toUpperCase()).toString();
        }
        return retval;
    }
}
