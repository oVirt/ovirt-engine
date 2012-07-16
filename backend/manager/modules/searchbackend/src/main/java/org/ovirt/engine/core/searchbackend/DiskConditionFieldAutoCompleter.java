package org.ovirt.engine.core.searchbackend;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
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
        mVerbs.put("FORMAT", "FORMAT");
        mVerbs.put("STATUS", "STATUS");
        mVerbs.put("DISK_TYPE", "DISK_TYPE");

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
        getTypeDictionary().put("FORMAT", VolumeFormat.class);
        getTypeDictionary().put("STATUS", ImageStatus.class);
        getTypeDictionary().put("DISK_TYPE", DiskStorageType.class);

        // building the ColumnName dict.
        mColumnNameDict.put("ALIAS", "disk_alias");
        mColumnNameDict.put("DESCRIPTION", "disk_description");
        mColumnNameDict.put("PROVISIONED_SIZE", "size");
        mColumnNameDict.put("SIZE", "size");
        mColumnNameDict.put("ACTUAL_SIZE", "actual_size");
        mColumnNameDict.put("CREATION_DATE", "creation_date");
        mColumnNameDict.put("BOOTABLE", "boot");
        mColumnNameDict.put("SHAREABLE", "shareable");
        mColumnNameDict.put("FORMAT", "volume_format");
        mColumnNameDict.put("STATUS", "imageStatus");
        mColumnNameDict.put("DISK_TYPE", "disk_storage_type");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        IAutoCompleter retval;
        if (StringHelper.EqOp(fieldName, "CREATIONDATE") || StringHelper.EqOp(fieldName, "SIZE")
                || StringHelper.EqOp(fieldName, "ACTUAL_SIZE")
                || StringHelper.EqOp(fieldName, "PROVISIONED_SIZE")) {
            retval = BiggerOrSmallerRelationAutoCompleter.INTSANCE;
        } else {
            retval = StringConditionRelationAutoCompleter.INSTANCE;
        }
        return retval;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (StringHelper.EqOp(fieldName, "FORMAT")) {
            retval = new EnumValueAutoCompleter(VolumeFormat.class);
        } else if (StringHelper.EqOp(fieldName, "STATUS")) {
            retval = new EnumValueAutoCompleter(ImageStatus.class);
        } else if (StringHelper.EqOp(fieldName, "DISK_TYPE")) {
            retval = new EnumValueAutoCompleter(DiskStorageType.class);
        }
        return retval;
    }

    @Override
    public void formatValue(String fieldName,
            RefObject<String> relations,
            RefObject<String> value,
            boolean caseSensitive) {
        if (StringHelper.EqOp(fieldName, "CREATIONDATE")) {
            Date tmp = new Date(Date.parse(StringHelper.trim(value.argvalue, '\'')));
            value.argvalue = StringFormat.format("'%1$s'", tmp);
        } else {
            super.formatValue(fieldName, relations, value, caseSensitive);
        }
    }
}
