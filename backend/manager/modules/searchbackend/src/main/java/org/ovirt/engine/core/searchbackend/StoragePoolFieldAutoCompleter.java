package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class StoragePoolFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public StoragePoolFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("DESCRIPTION", "DESCRIPTION");
        mVerbs.put("TYPE", "TYPE");
        mVerbs.put("STATUS", "STATUS");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("TYPE", StorageType.class);
        getTypeDictionary().put("STATUS", StoragePoolStatus.class);

        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "name");
        mColumnNameDict.put("DESCRIPTION", "description");
        mColumnNameDict.put("TYPE", "storage_pool_type");
        mColumnNameDict.put("STATUS", "status");

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
        // ORIGINAL LINE: case "STATUS":
        if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(StoragePoolStatus.class);
        }
        // ORIGINAL LINE: case "TYPE":
        else if (StringHelper.EqOp(fieldName, "TYPE")) {
            retval = new EnumValueAutoCompleter(StorageType.class);
        } else {
        }
        return retval;
    }
}
