package org.ovirt.engine.core.searchbackend;


public class ClusterConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public ClusterConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("DESCRIPTION", "DESCRIPTION");
        mVerbs.put("INITIALIZED", "INITIALIZED");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("INITIALIZED", Boolean.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "name");
        columnNameDict.put("DESCRIPTION", "description");
        // mColumnNameDict.put("INITIALIZED", "is_initialized");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if ("INITIALIZED".equals(fieldName)) {
            retval = new BitValueAutoCompleter();
        } else {
        }
        return retval;
    }
}
