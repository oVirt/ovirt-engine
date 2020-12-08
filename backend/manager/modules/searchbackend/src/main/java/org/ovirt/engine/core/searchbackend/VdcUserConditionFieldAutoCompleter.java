package org.ovirt.engine.core.searchbackend;

// IMPORTANT : Adding any new field to this class will require adding it to SearchObjectAutoCompleter.requiresFullTable Map

public class VdcUserConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String FIRST_NAME = "NAME";
    public static final String LAST_NAME = "LASTNAME";
    public static final String USER_NAME = "USRNAME";
    public static final String DIRECTORY = "DIRECTORY";
    public static final String LOGIN = "LOGIN";
    public static final String DEPARTMENT = "DEPARTMENT";
    public static final String TAG = "TAG";
    public static final String POOL = "POOL";
    public static final String TYPE = "TYPE";

    public enum UserOrGroup {
        User,
        Group
    }

    public VdcUserConditionFieldAutoCompleter() {
        super();
        // Building the basic verbs dictionary.
        verbs.add(FIRST_NAME);
        verbs.add(LAST_NAME);
        verbs.add(USER_NAME);
        verbs.add(LOGIN);
        verbs.add(DIRECTORY);
        verbs.add(DEPARTMENT);
        verbs.add(TAG);
        verbs.add(POOL);
        verbs.add(TYPE);

        // Building the auto completion dictionary.
        buildCompletions();
        // Building the types dictionary.
        getTypeDictionary().put(FIRST_NAME, String.class);
        getTypeDictionary().put(LAST_NAME, String.class);
        getTypeDictionary().put(USER_NAME, String.class);
        getTypeDictionary().put(LOGIN, String.class);
        getTypeDictionary().put(DIRECTORY, String.class);
        getTypeDictionary().put(DEPARTMENT, String.class);
        getTypeDictionary().put(TAG, String.class);
        getTypeDictionary().put(POOL, String.class);
        getTypeDictionary().put(TYPE, UserOrGroup.class);

        // building the ColumnName Dict
        columnNameDict.put(FIRST_NAME, "name");
        columnNameDict.put(LAST_NAME, "surname");
        columnNameDict.put(USER_NAME, "username");
        columnNameDict.put(LOGIN, "username");
        columnNameDict.put(DIRECTORY, "domain");
        columnNameDict.put(DEPARTMENT, "department");
        columnNameDict.put(TAG, "tag_name");
        columnNameDict.put(POOL, "vm_pool_name");
        columnNameDict.put(TYPE, "user_group");
        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
            return StringConditionRelationAutoCompleter.INSTANCE;
    }

    @Override
    public String buildConditionSql(
        String objName,
        String fieldName,
        String customizedValue,
        String customizedRelation,
        String tableName,
        boolean caseSensitive) {
        if (USER_NAME.equals(fieldName) && customizedValue.contains("@")) {
            // When the given user name contains the at sign, we need to split it and compare it to two columns in the
            // database: the column containing the login name of the user and the column containg the name of the
            // directory.
            int index = customizedValue.lastIndexOf("@");
            String loginValue = customizedValue.substring(0, index) + "'";
            String directoryValue = "'" + customizedValue.substring(index + 1);
            String loginSql = buildConditionSql(
                objName,
                LOGIN,
                loginValue,
                customizedRelation,
                tableName,
                caseSensitive
            );
            String directorySql = buildConditionSql(
                objName,
                DIRECTORY,
                directoryValue,
                customizedRelation,
                tableName,
                caseSensitive
            );
            return "(" + loginSql + " AND " + directorySql + ")";
        } else {
            return super.buildConditionSql(
                objName,
                fieldName,
                customizedValue,
                customizedRelation,
                tableName,
                caseSensitive
            );
        }
    }
}
