package org.ovirt.engine.core.searchbackend;


public class NetworkConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String COMMENT = "COMMENT";
    public static final String VLAN_ID = "VLANID";
    public static final String STP = "STP";
    public static final String MTU = "MTU";
    public static final String VM_NETWORK = "VMNETWORK";
    public static final String DATA_CENTER = "DATACENTER";
    public static final String LABEL = "LABEL";
    public static final String PROVIDER_NAME = "PROVIDER_NAME";

    public NetworkConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.add(NAME);
        mVerbs.add(DESCRIPTION);
        mVerbs.add(COMMENT);
        mVerbs.add(VLAN_ID);
        mVerbs.add(STP);
        mVerbs.add(MTU);
        mVerbs.add(VM_NETWORK);
        mVerbs.add(DATA_CENTER);
        mVerbs.add(LABEL);
        mVerbs.add(PROVIDER_NAME);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(VLAN_ID, Integer.class);
        getTypeDictionary().put(STP, Boolean.class);
        getTypeDictionary().put(MTU, Integer.class);
        getTypeDictionary().put(VM_NETWORK, Boolean.class);
        getTypeDictionary().put(DATA_CENTER, String.class);
        getTypeDictionary().put(LABEL, String.class);
        getTypeDictionary().put(PROVIDER_NAME, String.class);

        // building the ColumnName dict.
        columnNameDict.put(NAME, "name");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(COMMENT, "free_text_comment");
        columnNameDict.put(VLAN_ID, "vlan_id");
        columnNameDict.put(STP, "stp");
        columnNameDict.put(MTU, "mtu");
        columnNameDict.put(VM_NETWORK, "vm_network");
        columnNameDict.put(DATA_CENTER, "storage_pool_name");
        columnNameDict.put(LABEL, "label");
        columnNameDict.put(PROVIDER_NAME, "provider_name");

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
