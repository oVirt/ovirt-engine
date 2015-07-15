package org.ovirt.engine.core.searchbackend;

public class NetworkHostConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    private static final String HOST_NAME = "HOST_NAME";

    public NetworkHostConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(HOST_NAME);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(HOST_NAME, String.class);

        // building the ColumnName dict.
        columnNameDict.put(HOST_NAME, "vds_name");

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
