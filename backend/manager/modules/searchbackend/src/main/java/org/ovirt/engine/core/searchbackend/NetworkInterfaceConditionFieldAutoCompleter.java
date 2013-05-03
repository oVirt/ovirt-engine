package org.ovirt.engine.core.searchbackend;


public class NetworkInterfaceConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    private static final String NETWORK_NAME = "NETWORK_NAME";

    public NetworkInterfaceConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.add(NETWORK_NAME);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NETWORK_NAME, String.class);

        // building the ColumnName dict.
        columnNameDict.put(NETWORK_NAME, "network_name");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        final Class<?> clazz = getTypeDictionary().get(fieldName);
        if (clazz == Integer.class) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        }
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

}
