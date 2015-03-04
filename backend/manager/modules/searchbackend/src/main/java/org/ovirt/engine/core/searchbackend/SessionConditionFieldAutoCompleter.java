package org.ovirt.engine.core.searchbackend;

import java.util.UUID;

public class SessionConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";

    public SessionConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        mVerbs.add(USER_ID);
        mVerbs.add(USER_NAME);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(USER_ID, UUID.class);
        getTypeDictionary().put(USER_NAME, String.class);

        // building the ColumnName dict.
        columnNameDict.put(USER_ID, USER_ID);
        columnNameDict.put(USER_NAME, USER_NAME);

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
