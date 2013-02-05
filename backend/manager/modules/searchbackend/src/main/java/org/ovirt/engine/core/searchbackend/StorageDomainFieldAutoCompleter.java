package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;

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
        columnNameDict.put("NAME", "storage_name");
        columnNameDict.put("STATUS", "storage_domain_shared_status");
        columnNameDict.put("DATACENTER", "storage_pool_name");
        columnNameDict.put("TYPE", "storage_type");
        columnNameDict.put("SIZE", "available_disk_size");
        columnNameDict.put("USED", "used_disk_size");
        columnNameDict.put("COMMITTED", "commited_disk_size");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("SIZE".equals(fieldName) || "USED".equals(fieldName)
                || "COMMITTED".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if ("TYPE".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageType.class);
        }
        else if ("STATUS".equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageDomainSharedStatus.class);
        } else {
        }
        return retval;
    }
}
