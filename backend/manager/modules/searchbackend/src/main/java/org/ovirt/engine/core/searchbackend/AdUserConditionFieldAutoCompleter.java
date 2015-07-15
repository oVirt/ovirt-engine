package org.ovirt.engine.core.searchbackend;

import java.util.Collections;

public class AdUserConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String NAME = "NAME";
    public static final String USERNAME = "USERNAME";
    public static final String ALLNAMES = "ALLNAMES";
    public static final String LASTNAME = "LASTNAME";
    public static final String DEPARTMENT = "DEPARTMENT";
    public static final String TITLE = "TITLE";
    public static final String LDAP = "LDAP";

    public AdUserConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        verbs.add(NAME);
        verbs.add(USERNAME);
        verbs.add(ALLNAMES);

        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(USERNAME, String.class);
        // building the ColumnName Dict
        columnNameDict.put(NAME, "$GIVENNAME");
        columnNameDict.put(USERNAME, "$SAMACCOUNTNAME");

        if (LDAP.equalsIgnoreCase(SyntaxCheckerFactory.getConfigAuthenticationMethod())) {
            verbs.add(LASTNAME);
            verbs.add(DEPARTMENT);
            verbs.add(TITLE);

            getTypeDictionary().put(LASTNAME, String.class);
            getTypeDictionary().put(DEPARTMENT, String.class);
            getTypeDictionary().put(TITLE, String.class);

            columnNameDict.put(LASTNAME, "$SN");
            columnNameDict.put(DEPARTMENT, "$DEPARTMENT");
            columnNameDict.put(TITLE, "$TITLE");
        }

        // Building the autoCompletion Dict
        buildCompletions();

        // Building the validation dict
        for (String key : verbs) {
            validationDict.put(key, Collections.singletonList(validCharacters));
        }
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
