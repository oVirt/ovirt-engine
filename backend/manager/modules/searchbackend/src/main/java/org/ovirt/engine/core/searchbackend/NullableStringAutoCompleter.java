package org.ovirt.engine.core.searchbackend;

public class NullableStringAutoCompleter implements IConditionValueAutoCompleter {

    @Override
    public String[] getCompletion(String wordPart) {
        return new String[]{"", "null"};
    }

    @Override
    public boolean validate(String text) {
        return true;
    }

    @Override
    public boolean validateCompletion(String text) {
        return true;
    }

    @Override
    public String changeCaseDisplay(String text) {
        return text.toLowerCase();
    }

    @Override
    public String convertFieldEnumValueToActualValue(String fieldValue) {
        if("null".equalsIgnoreCase(fieldValue)) {
            return null;
        } else {
            return fieldValue;
        }
    }

}
