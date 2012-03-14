package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.List;

public class AdGroupConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public AdGroupConditionFieldAutoCompleter() {
        super();
        // Building the basic vervs Dict
        mVerbs.put("NAME", "NAME");

        // Building the autoCompletion Dict
        buildCompletions();

        // Building the types dict
        getTypeDictionary().put("NAME", String.class);

        // building the ColumnName Dict
        mColumnNameDict.put("NAME", "$CN");

        // Building the validation dict
        ValueValidationFunction charValidation = validCahracters;
        for (String key : mVerbs.keySet()) {
            List<ValueValidationFunction> curList = new ArrayList<ValueValidationFunction>(1);
            curList.add(charValidation);
            mValidationDict.put(key, curList);
        }
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
