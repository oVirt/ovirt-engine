package org.ovirt.engine.core.searchbackend;

public interface IConditionValueAutoCompleter extends IAutoCompleter {
    String convertFieldEnumValueToActualValue(String fieldValue);
}
