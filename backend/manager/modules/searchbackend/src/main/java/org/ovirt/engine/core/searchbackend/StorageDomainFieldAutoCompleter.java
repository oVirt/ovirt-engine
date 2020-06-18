package org.ovirt.engine.core.searchbackend;

import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageDomainFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String STATUS = "STATUS";
    public static final String SHARED_STATUS = "SHARED_STATUS";
    public static final String EXTERNAL_STATUS = "EXTERNAL_STATUS";
    public static final String DATACENTER = "DATACENTER";
    public static final String TYPE = "TYPE";
    public static final String FREE_SIZE = "FREE_SIZE";
    public static final String USED_SIZE = "USED_SIZE";
    public static final String TOTAL_SIZE = "TOTAL_SIZE";
    public static final String COMMITTED = "COMMITTED";
    public static final String COMMENT = "COMMENT";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String WIPE_AFTER_DELETE = "WIPE_AFTER_DELETE";
    public static final String DISCARD_AFTER_DELETE = "DISCARD_AFTER_DELETE";
    public static final String LOW_SPACE_THRESHOLD = "LOW_SPACE_THRESHOLD (%)";
    public static final String CRITICAL_SPACE_THRESHOLD = "CRITICAL_SPACE_THRESHOLD (GB)";
    public static final String BACKUP = "BACKUP";

    public StorageDomainFieldAutoCompleter() {
        // Building the basic vervs Dict
        verbs.add(NAME);
        verbs.add(STATUS);
        verbs.add(SHARED_STATUS);
        verbs.add(EXTERNAL_STATUS);
        verbs.add(DATACENTER);
        verbs.add(TYPE);
        verbs.add(FREE_SIZE);
        verbs.add(USED_SIZE);
        verbs.add(TOTAL_SIZE);
        verbs.add(COMMITTED);
        verbs.add(COMMENT);
        verbs.add(DESCRIPTION);
        verbs.add(WIPE_AFTER_DELETE);
        verbs.add(DISCARD_AFTER_DELETE);
        verbs.add(LOW_SPACE_THRESHOLD);
        verbs.add(CRITICAL_SPACE_THRESHOLD);
        verbs.add(BACKUP);

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(STATUS, StorageDomainStatus.class);
        getTypeDictionary().put(SHARED_STATUS, StorageDomainSharedStatus.class);
        getTypeDictionary().put(EXTERNAL_STATUS, ExternalStatus.class);
        getTypeDictionary().put(DATACENTER, String.class);
        getTypeDictionary().put(TYPE, StorageType.class);
        getTypeDictionary().put(FREE_SIZE, Integer.class);
        getTypeDictionary().put(USED_SIZE, Integer.class);
        getTypeDictionary().put(TOTAL_SIZE, Integer.class);
        getTypeDictionary().put(COMMITTED, Integer.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(WIPE_AFTER_DELETE, Boolean.class);
        getTypeDictionary().put(DISCARD_AFTER_DELETE, Boolean.class);
        getTypeDictionary().put(LOW_SPACE_THRESHOLD, Integer.class);
        getTypeDictionary().put(CRITICAL_SPACE_THRESHOLD, Integer.class);
        getTypeDictionary().put(BACKUP, Boolean.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "storage_name");
        columnNameDict.put(STATUS, "status");
        columnNameDict.put(SHARED_STATUS, "storage_domain_shared_status");
        columnNameDict.put(EXTERNAL_STATUS, "external_status");
        columnNameDict.put(DATACENTER, "storage_pool_name::text");
        columnNameDict.put(TYPE, "storage_type");
        columnNameDict.put(FREE_SIZE, "available_disk_size");
        columnNameDict.put(USED_SIZE, "used_disk_size");
        columnNameDict.put(TOTAL_SIZE, "total_disk_size");
        columnNameDict.put(COMMITTED, "commited_disk_size");
        columnNameDict.put(COMMENT, "storage_comment");
        columnNameDict.put(DESCRIPTION, "storage_description");
        columnNameDict.put(WIPE_AFTER_DELETE, "wipe_after_delete");
        columnNameDict.put(DISCARD_AFTER_DELETE, "discard_after_delete");
        columnNameDict.put(LOW_SPACE_THRESHOLD, "warning_low_space_indicator");
        columnNameDict.put(CRITICAL_SPACE_THRESHOLD, "critical_space_action_blocker");
        columnNameDict.put(BACKUP, "backup");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (FREE_SIZE.equals(fieldName) || USED_SIZE.equals(fieldName)
                || COMMITTED.equals(fieldName) || TOTAL_SIZE.equals(fieldName)) {
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
        } else if (STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageDomainStatus.class);
        } else if (SHARED_STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StorageDomainSharedStatus.class);
        } else if (EXTERNAL_STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(ExternalStatus.class);
        } else if (Stream.of(WIPE_AFTER_DELETE, DISCARD_AFTER_DELETE, BACKUP)
                .anyMatch(s -> s.equals(fieldName))) {
            retval = new BitValueAutoCompleter();
        }
        return retval;
    }
}
