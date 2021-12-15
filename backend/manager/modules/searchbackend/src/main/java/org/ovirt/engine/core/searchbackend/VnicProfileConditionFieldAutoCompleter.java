package org.ovirt.engine.core.searchbackend;


public class VnicProfileConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String PORT_MIRRORING = "PORT_MIRRORING";
    public static final String PASSTHROUGH = "PASSTHROUGH";
    public static final String NETWORK_NAME = "NETWORK_NAME";
    public static final String COMPAT_VERSION = "COMPATIBILITY_VERSION";
    public static final String DATA_CENTER = "DATACENTER";
    public static final String QOS = "QOS_NAME";
    public static final String NETWORK_FILTER = "NETWORK_FILTER";
    public static final String FAILOVER = "FAILOVER";

    public VnicProfileConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(NAME);
        verbs.add(DESCRIPTION);
        verbs.add(PORT_MIRRORING);
        verbs.add(PASSTHROUGH);
        verbs.add(NETWORK_NAME);
        verbs.add(COMPAT_VERSION);
        verbs.add(DATA_CENTER);
        verbs.add(QOS);
        verbs.add(NETWORK_FILTER);
        verbs.add(FAILOVER);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(PORT_MIRRORING, Boolean.class);
        getTypeDictionary().put(PASSTHROUGH, Boolean.class);
        getTypeDictionary().put(NETWORK_NAME, String.class);
        getTypeDictionary().put(COMPAT_VERSION, String.class);
        getTypeDictionary().put(DATA_CENTER, String.class);
        getTypeDictionary().put(QOS, String.class);
        getTypeDictionary().put(NETWORK_FILTER, String.class);
        getTypeDictionary().put(FAILOVER, String.class);
        // building the ColumnName dict.
        columnNameDict.put(NAME, "name");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(PORT_MIRRORING, "port_mirroring");
        columnNameDict.put(PASSTHROUGH, "passthrough");
        columnNameDict.put(NETWORK_NAME, "network_name");
        columnNameDict.put(COMPAT_VERSION, "compatibility_version");
        columnNameDict.put(DATA_CENTER, "data_center_name");
        columnNameDict.put(QOS, "network_qos_name");
        columnNameDict.put(NETWORK_FILTER, "network_filter_name");
        columnNameDict.put(FAILOVER, "failover_vnic_profile_name");

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
        if (PORT_MIRRORING.equals(fieldName) || PASSTHROUGH.equals(fieldName)) {
            completer = new BitValueAutoCompleter();
        }
        return completer;
    }

}
