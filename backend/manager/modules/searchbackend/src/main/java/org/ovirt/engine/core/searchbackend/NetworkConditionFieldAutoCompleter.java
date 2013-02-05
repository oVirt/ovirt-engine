package org.ovirt.engine.core.searchbackend;


public class NetworkConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String VLAN_ID = "VLANID";
    private static final String STP = "STP";
    private static final String MTU = "MTU";
    private static final String VM_NETWORK = "VMNETWORK";
    private static final String DATA_CENTER = "DATACENTER";

    public NetworkConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.put(NAME, NAME);
        mVerbs.put(DESCRIPTION, DESCRIPTION);
        mVerbs.put(VLAN_ID, VLAN_ID);
        mVerbs.put(STP, STP);
        mVerbs.put(MTU, MTU);
        mVerbs.put(VM_NETWORK, VM_NETWORK);
        mVerbs.put(DATA_CENTER, DATA_CENTER);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(VLAN_ID, Integer.class);
        getTypeDictionary().put(STP, Boolean.class);
        getTypeDictionary().put(MTU, Integer.class);
        getTypeDictionary().put(VM_NETWORK, Boolean.class);
        getTypeDictionary().put(DATA_CENTER, String.class);

        // building the ColumnName dict.
        columnNameDict.put(NAME, "name");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(VLAN_ID, "vlan_id");
        columnNameDict.put(STP, "stp");
        columnNameDict.put(MTU, "mtu");
        columnNameDict.put(VM_NETWORK, "vm_network");
        columnNameDict.put(DATA_CENTER, "storage_pool_name");

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

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter completer = null;
        if (STP.equals(fieldName) || VM_NETWORK.equals(fieldName)) {
            completer = new BitValueAutoCompleter();
        }
        return completer;
    }

}
