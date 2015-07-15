package org.ovirt.engine.core.searchbackend;


public class NetworkInterfaceConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    private static final String NETWORK_NAME = "NETWORK_NAME";
    private static final String MAC_ADDR = "MAC";

    public NetworkInterfaceConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(NETWORK_NAME);
        verbs.add(MAC_ADDR);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NETWORK_NAME, String.class);
        getTypeDictionary().put(MAC_ADDR, String.class);

        // building the ColumnName dict.
        columnNameDict.put(NETWORK_NAME, "network_name");
        columnNameDict.put(MAC_ADDR, "mac_addr");

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
