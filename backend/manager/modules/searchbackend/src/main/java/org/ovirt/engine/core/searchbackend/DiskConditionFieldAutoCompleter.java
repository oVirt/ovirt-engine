package org.ovirt.engine.core.searchbackend;

import java.util.Date;
import java.util.UUID;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;

public class DiskConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String ALIAS = "ALIAS";
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String PROVISIONED_SIZE = "PROVISIONED_SIZE";
    public static final String SIZE = "SIZE";
    public static final String ACTUAL_SIZE = "ACTUAL_SIZE";
    public static final String CREATION_DATE = "CREATION_DATE";
    public static final String SHAREABLE = "SHAREABLE";
    public static final String STATUS = "STATUS";
    public static final String DISK_TYPE = "DISK_TYPE";
    public static final String DISK_CONTENT_TYPE = "DISK_CONTENT_TYPE";
    public static final String ALLOCATION_POLICY = "ALLOCATION_POLICY";
    public static final String NUMBER_OF_VMS = "NUMBER_OF_VMS";
    public static final String VM_NAMES = "VM_NAMES";
    public static final String QUOTA = "QUOTA";
    public static final String ID = "ID";
    public static final String WIPE_AFTER_DELETE = "WIPE_AFTER_DELETE";
    public static final String LAST_MODIFIED = "LAST_MODIFIED";

    public DiskConditionFieldAutoCompleter() {
        // Building the basic verbs set.
        verbs.add(ALIAS);
        verbs.add(NAME);
        verbs.add(DESCRIPTION);
        verbs.add(PROVISIONED_SIZE);
        verbs.add(SIZE);
        verbs.add(ACTUAL_SIZE);
        verbs.add(CREATION_DATE);
        verbs.add(SHAREABLE);
        verbs.add(STATUS);
        verbs.add(DISK_TYPE);
        verbs.add(DISK_CONTENT_TYPE);
        verbs.add(ALLOCATION_POLICY);
        verbs.add(NUMBER_OF_VMS);
        verbs.add(VM_NAMES);
        verbs.add(QUOTA);
        verbs.add(ID);
        verbs.add(WIPE_AFTER_DELETE);
        verbs.add(LAST_MODIFIED);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(ALIAS, String.class);
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(PROVISIONED_SIZE, Long.class);
        getTypeDictionary().put(SIZE, Long.class);
        getTypeDictionary().put(ACTUAL_SIZE, Long.class);
        getTypeDictionary().put(CREATION_DATE, Date.class);
        getTypeDictionary().put(SHAREABLE, Boolean.class);
        getTypeDictionary().put(STATUS, ImageStatus.class);
        getTypeDictionary().put(DISK_TYPE, DiskStorageType.class);
        getTypeDictionary().put(DISK_CONTENT_TYPE, DiskContentType.class);
        getTypeDictionary().put(ALLOCATION_POLICY, VolumeType.class);
        getTypeDictionary().put(NUMBER_OF_VMS, Integer.class);
        getTypeDictionary().put(VM_NAMES, String.class);
        getTypeDictionary().put(QUOTA, String.class);
        getTypeDictionary().put(ID, UUID.class);
        getTypeDictionary().put(WIPE_AFTER_DELETE, Boolean.class);
        getTypeDictionary().put(LAST_MODIFIED, Date.class);

        // building the ColumnName dict. - the name of the column in db
        columnNameDict.put(ALIAS, "disk_alias");
        columnNameDict.put(NAME, "disk_alias");
        columnNameDict.put(DESCRIPTION, "disk_description");
        columnNameDict.put(PROVISIONED_SIZE, "size");
        columnNameDict.put(SIZE, "size");
        columnNameDict.put(ACTUAL_SIZE, "actual_size");
        columnNameDict.put(CREATION_DATE, "creation_date");
        columnNameDict.put(SHAREABLE, "shareable");
        columnNameDict.put(STATUS, "imageStatus");
        columnNameDict.put(DISK_TYPE, "disk_storage_type");
        columnNameDict.put(DISK_CONTENT_TYPE, "disk_content_type");
        columnNameDict.put(ALLOCATION_POLICY, "volume_type");
        columnNameDict.put(NUMBER_OF_VMS, "number_of_vms");
        columnNameDict.put(VM_NAMES, "vm_names");
        columnNameDict.put(QUOTA, "quota_name");
        columnNameDict.put(ID, "disk_id");
        columnNameDict.put(WIPE_AFTER_DELETE, "wipe_after_delete");
        columnNameDict.put(LAST_MODIFIED, "lastModified");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if (CREATION_DATE.equals(fieldName)
                || SIZE.equals(fieldName)
                || ACTUAL_SIZE.equals(fieldName)
                || PROVISIONED_SIZE.equals(fieldName)
                || LAST_MODIFIED.equals(fieldName)) {
            return BiggerOrSmallerRelationAutoCompleter.INSTANCE;
        } else if (NUMBER_OF_VMS.equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (STATUS.equals(fieldName)) {
            return new EnumValueAutoCompleter(ImageStatus.class);
        } else if (DISK_TYPE.equals(fieldName)) {
            return new EnumValueAutoCompleter(DiskStorageType.class);
        } else if (DISK_CONTENT_TYPE.equals(fieldName)) {
            return new EnumValueAutoCompleter(DiskContentType.class);
        } else if (ALLOCATION_POLICY.equals(fieldName)) {
            return new EnumValueAutoCompleter(VolumeType.class);
        } else if (SHAREABLE.equals(fieldName) ||
                WIPE_AFTER_DELETE.equals(fieldName)) {
            return new BitValueAutoCompleter();
        }
        return null;
    }

    @Override
    public void formatValue(String fieldName, Pair<String, String> pair, boolean caseSensitive) {
        if (CREATION_DATE.equals(fieldName) || LAST_MODIFIED.equals(fieldName)) {
            Date tmp = new Date(Date.parse(StringHelper.trim(pair.getSecond(), '\'')));
            pair.setSecond(StringFormat.format("'%1$s'", tmp));
        } else {
            super.formatValue(fieldName, pair, caseSensitive);
        }
    }
}
