package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;

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
        columnNameDict.put("NAME", "name");
        columnNameDict.put("DESCRIPTION", "description");
        columnNameDict.put("TYPE", "storage_pool_type");
        columnNameDict.put("STATUS", "status");

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
        if ("STATUS".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StoragePoolStatus.class);
        }
        else if ("TYPE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageType.class);
        } else {
        }
        return retval;
    }
}
