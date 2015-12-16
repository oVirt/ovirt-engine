package org.ovirt.engine.core.searchbackend;

/**
 * Auto completer that presents enum names as completions, and returns the same as actual value. This is useful for
 * enums that are stored as varchars in DB, with value same as the name.
 */
public class EnumNameAutoCompleter extends BaseAutoCompleter implements IConditionValueAutoCompleter {

    /**
     * Constructor adds all names of the enum to completion dictionary
     */
    public <T extends Enum<T>> EnumNameAutoCompleter(Class<T> enumerationType) {
        for (T enumMember : enumerationType.getEnumConstants()) {
            verbs.add(enumMember.name());
        }
        buildCompletions();
    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        return fieldValue.toUpperCase();
    }
}
