package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

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
        mColumnNameDict.put("NAME", "vm_pool_name");
        mColumnNameDict.put("DESCRIPTION", "vm_pool_description");
        mColumnNameDict.put("TYPE", "vm_pool_type");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        IAutoCompleter retval;
        // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (fieldName)
        {
            retval = new StringConditionRelationAutoCompleter();
        }

        return retval;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (fieldName)
        // ORIGINAL LINE: case "TYPE":
        if (StringHelper.EqOp(fieldName, "TYPE")) {
            retval = new EnumValueAutoCompleter(VmPoolType.class);
        } else {
        }
        return retval;
    }
}
