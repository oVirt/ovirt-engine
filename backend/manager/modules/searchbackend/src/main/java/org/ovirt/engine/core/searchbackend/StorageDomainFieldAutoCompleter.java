package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class StorageDomainFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public StorageDomainFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("DATACENTER", "DATACENTER");
        mVerbs.put("TYPE", "TYPE");
        mVerbs.put("SIZE", "SIZE");
        mVerbs.put("USED", "USED");
        mVerbs.put("COMMITTED", "COMMITTED");

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put("NAME", String.class);
        getTypeDictionary().put("STATUS", StorageDomainStatus.class);
        getTypeDictionary().put("DATACENTER", String.class);
        getTypeDictionary().put("TYPE", StorageType.class);
        getTypeDictionary().put("SIZE", Integer.class);
        getTypeDictionary().put("USED", Integer.class);
        getTypeDictionary().put("COMMITTED", Integer.class);

        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "storage_name");
        mColumnNameDict.put("STATUS", "storage_domain_shared_status");
        mColumnNameDict.put("DATACENTER", "storage_pool_name");
        mColumnNameDict.put("TYPE", "storage_type");
        mColumnNameDict.put("SIZE", "available_disk_size");
        mColumnNameDict.put("USED", "used_disk_size");
        mColumnNameDict.put("COMMITTED", "commited_disk_size");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (StringHelper.EqOp(fieldName, "SIZE") || StringHelper.EqOp(fieldName, "USED")
                || StringHelper.EqOp(fieldName, "COMMITTED")) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "TYPE")) {
            retval = new EnumValueAutoCompleter(StorageType.class);
        }
        else if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(StorageDomainSharedStatus.class);
        } else {
        }
        return retval;
    }
}
