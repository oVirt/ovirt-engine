package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.EnumCompat;

/**
 * Auto completer that presents enum names as completions, and returns the same as actual value. This is useful for
 * enums that are stored as varchars in DB, with value same as the name.
 */
public class EnumNameAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {

    /**
     * Constructor adds all names of the enum to completion dictionary
     * @param enumerationType
     */
    public <T extends Enum<T>> EnumNameAutoCompleter(Class<T> enumerationType) {
        for (String name : EnumCompat.GetNames(enumerationType)) {
            mVerbs.put(name, name);
        }
        buildCompletions();
    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        return fieldValue.toUpperCase();
    }
}
