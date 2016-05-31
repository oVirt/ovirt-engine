package org.ovirt.engine.core.searchbackend;

import java.util.UUID;

public class SessionConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String AUTHZ_NAME = "AUTHZ_NAME";
    public static final String SOURCE_IP = "SOURCE_IP";
    public static final String SESSION_DB_ID = "ID";

    public SessionConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(USER_ID);
        verbs.add(USER_NAME);
        verbs.add(AUTHZ_NAME);
        verbs.add(SOURCE_IP);
        verbs.add(SESSION_DB_ID);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(USER_ID, UUID.class);
        getTypeDictionary().put(USER_NAME, String.class);
        getTypeDictionary().put(AUTHZ_NAME, String.class);
        getTypeDictionary().put(SOURCE_IP, String.class);
        getTypeDictionary().put(SESSION_DB_ID, Integer.class);

        // building the ColumnName dict.
        columnNameDict.put(USER_ID, USER_ID);
        columnNameDict.put(USER_NAME, USER_NAME);
        columnNameDict.put(AUTHZ_NAME, AUTHZ_NAME);
        columnNameDict.put(SOURCE_IP, SOURCE_IP);
        columnNameDict.put(SESSION_DB_ID, SESSION_DB_ID);

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
