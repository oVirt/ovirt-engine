package org.ovirt.engine.core.searchbackend;

import java.util.Collections;

public class AdGroupConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String NAME = "NAME";
    public static final String GROUPNAME = "GROUPNAME";
    public static final String ALLNAMES = "ALLNAMES";

    public AdGroupConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        verbs.add(NAME);
        verbs.add(GROUPNAME);
        verbs.add(ALLNAMES);

        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(GROUPNAME, String.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "$CN");
        columnNameDict.put(GROUPNAME, "$CN");

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
