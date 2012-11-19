package org.ovirt.engine.core.searchbackend;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;

public class DiskConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public DiskConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.put("ALIAS", "ALIAS");
        mVerbs.put("DESCRIPTION", "DESCRIPTION");
        mVerbs.put("PROVISIONED_SIZE", "PROVISIONED_SIZE");
        mVerbs.put("SIZE", "SIZE");
        mVerbs.put("ACTUAL_SIZE", "ACTUAL_SIZE");
        mVerbs.put("CREATION_DATE", "CREATION_DATE");
        mVerbs.put("BOOTABLE", "BOOTABLE");
        mVerbs.put("SHAREABLE", "SHAREABLE");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("DISK_TYPE", "DISK_TYPE");
        mVerbs.put("NUMBER_OF_VMS", "NUMBER_OF_VMS");
        mVerbs.put("VM_NAMES", "VM_NAMES");
        mVerbs.put("QUOTA", "QUOTA");

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put("ALIAS", String.class);
        getTypeDictionary().put("DESCRIPTION", String.class);
        getTypeDictionary().put("PROVISIONED_SIZE", Long.class);
        getTypeDictionary().put("SIZE", Long.class);
        getTypeDictionary().put("ACTUAL_SIZE", Long.class);
        getTypeDictionary().put("CREATION_DATE", Date.class);
        getTypeDictionary().put("BOOTABLE", Boolean.class);
        getTypeDictionary().put("SHAREABLE", Boolean.class);
        getTypeDictionary().put("STATUS", ImageStatus.class);
        getTypeDictionary().put("DISK_TYPE", DiskStorageType.class);
        getTypeDictionary().put("NUMBER_OF_VMS", Integer.class);
        getTypeDictionary().put("VM_NAMES", String.class);
        getTypeDictionary().put("QUOTA", String.class);


        // building the ColumnName dict.
        mColumnNameDict.put("ALIAS", "disk_alias");
        mColumnNameDict.put("DESCRIPTION", "disk_description");
        mColumnNameDict.put("PROVISIONED_SIZE", "size");
        mColumnNameDict.put("SIZE", "size");
        mColumnNameDict.put("ACTUAL_SIZE", "actual_size");
        mColumnNameDict.put("CREATION_DATE", "creation_date");
        mColumnNameDict.put("BOOTABLE", "boot");
        mColumnNameDict.put("SHAREABLE", "shareable");
        mColumnNameDict.put("STATUS", "imageStatus");
        mColumnNameDict.put("DISK_TYPE", "disk_storage_type");
        mColumnNameDict.put("NUMBER_OF_VMS", "number_of_vms");
        mColumnNameDict.put("VM_NAMES", "vm_names");
        mColumnNameDict.put("QUOTA", "quota_name");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        if ("CREATIONDATE".equals(fieldName) || "SIZE".equals(fieldName)
                || "ACTUAL_SIZE".equals(fieldName)
                || "PROVISIONED_SIZE".equals(fieldName)) {
            return BiggerOrSmallerRelationAutoCompleter.INTSANCE;
        }
        else if ("NUMBER_OF_VMS".equals(fieldName)) {
            return NumericConditionRelationAutoCompleter.INSTANCE;
        } else {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if ("STATUS".equals(fieldName)) {
            return new EnumValueAutoCompleter(ImageStatus.class);
        } else if ("DISK_TYPE".equals(fieldName)) {
            return new EnumValueAutoCompleter(DiskStorageType.class);
        } else if ("BOOTABLE".equals(fieldName) ||
                   "SHAREABLE".equals(fieldName) ) {
            return new BitValueAutoCompleter();
        }
        return null;
    }

    @Override
    public void formatValue(String fieldName,
            RefObject<String> relations,
            RefObject<String> value,
            boolean caseSensitive) {
        if ("CREATIONDATE".equals(fieldName)) {
            Date tmp = new Date(Date.parse(StringHelper.trim(value.argvalue, '\'')));
            value.argvalue = StringFormat.format("'%1$s'", tmp);
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}
