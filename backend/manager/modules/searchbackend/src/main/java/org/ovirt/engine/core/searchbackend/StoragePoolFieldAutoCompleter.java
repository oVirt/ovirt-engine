package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;

public class StoragePoolFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String LOCAL = "LOCAL";
    public static final String STATUS = "STATUS";
    public static final String COMMENT = "COMMENT";
    public static final String COMPATIBILITY_VERSION = "COMPATIBILITY_VERSION";

    public StoragePoolFieldAutoCompleter() {
        // Building the basic vervs Dict
        verbs.add(NAME);
        verbs.add(DESCRIPTION);
        verbs.add(LOCAL);
        verbs.add(STATUS);
        verbs.add(COMMENT);
        verbs.add(COMPATIBILITY_VERSION);

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(LOCAL, Boolean.class);
        getTypeDictionary().put(STATUS, StoragePoolStatus.class);
        getTypeDictionary().put(COMPATIBILITY_VERSION, String.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "name");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(COMMENT, "free_text_comment");
        columnNameDict.put(LOCAL, "is_local");
        columnNameDict.put(STATUS, "status");
        columnNameDict.put(COMPATIBILITY_VERSION, "compatibility_version");

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
        if (STATUS.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(StoragePoolStatus.class);
        } else if (LOCAL.equals(fieldName)) {
            retval = new BitValueAutoCompleter();
        }
        return retval;
    }
}
