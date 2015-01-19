package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;

public class StorageDomainFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String STATUS = "STATUS";
    public static final String DATACENTER = "DATACENTER";
    public static final String TYPE = "TYPE";
    public static final String SIZE = "SIZE";
    public static final String USED = "USED";
    public static final String COMMITTED = "COMMITTED";
    public static final String COMMENT = "COMMENT";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String WIPE_AFTER_DELETE = "WIPE_AFTER_DELETE";


    public StorageDomainFieldAutoCompleter() {
        // Building the basic vervs Dict
        mVerbs.add(NAME);
        mVerbs.add(STATUS);
        mVerbs.add(DATACENTER);
        mVerbs.add(TYPE);
        mVerbs.add(SIZE);
        mVerbs.add(USED);
        mVerbs.add(COMMITTED);
        mVerbs.add(COMMENT);
        mVerbs.add(DESCRIPTION);
        mVerbs.add(WIPE_AFTER_DELETE);

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(STATUS, StorageDomainStatus.class);
        getTypeDictionary().put(DATACENTER, String.class);
        getTypeDictionary().put(TYPE, StorageType.class);
        getTypeDictionary().put(SIZE, Integer.class);
        getTypeDictionary().put(USED, Integer.class);
        getTypeDictionary().put(COMMITTED, Integer.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(WIPE_AFTER_DELETE, Boolean.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "storage_name");
        columnNameDict.put(STATUS, "storage_domain_shared_status");
        columnNameDict.put(DATACENTER, "storage_pool_name::text");
        columnNameDict.put(TYPE, "storage_type");
        columnNameDict.put(SIZE, "available_disk_size");
        columnNameDict.put(USED, "used_disk_size");
        columnNameDict.put(COMMITTED, "commited_disk_size");
        columnNameDict.put(COMMENT, "storage_comment");
        columnNameDict.put(DESCRIPTION, "storage_description");
        columnNameDict.put(WIPE_AFTER_DELETE, "wipe_after_delete");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (SIZE.equals(fieldName) || USED.equals(fieldName)
                || COMMITTED.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (TYPE.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageType.class);
        }
        else if (STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageDomainSharedStatus.class);
        }
        else if (WIPE_AFTER_DELETE.equals(fieldName)) {
            retval = new BitValueAutoCompleter();
        } else {
        }
        return retval;
    }
}
