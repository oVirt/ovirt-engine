package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.VmPoolType;

public class PoolConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public PoolConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("DESCRIPTION", "DESCRIPTION");
        mVerbs.put("TYPE", "TYPE");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("TYPE", VmPoolType.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "vm_pool_name");
        columnNameDict.put("DESCRIPTION", "vm_pool_description");
        columnNameDict.put("TYPE", "vm_pool_type");

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
        if ("TYPE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(VmPoolType.class);
        }
        return retval;
    }
}
