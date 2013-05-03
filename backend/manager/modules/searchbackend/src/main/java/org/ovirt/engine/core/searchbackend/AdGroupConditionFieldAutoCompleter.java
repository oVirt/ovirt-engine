package org.ovirt.engine.core.searchbackend;

import java.util.Collections;

public class AdGroupConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public AdGroupConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.add("NAME");

        // Building the autoCompletion Dict
        buildCompletions();

        // Building the types dict
        getTypeDictionary().put("NAME", String.class);

        // building the ColumnName Dict
        columnNameDict.put("NAME", "$CN");

        // Building the validation dict
        for (String key : mVerbs) {
            validationDict.put(key, Collections.singletonList(validCharacters));
        }
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
