package org.ovirt.engine.core.searchbackend;

//C# TO JAVA CONVERTER TODO TASK: Delegates are not available in Java:
// public delegate bool valueValidationFunction(string field, string value);

public interface IConditionValueAutoCompleter extends IAutoCompleter {
    String convertFieldEnumValueToActualValue(String fieldValue);
}
