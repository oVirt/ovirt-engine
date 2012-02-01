package org.ovirt.engine.core.searchbackend;

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
        valueValidationFunction charValidation = new valueValidationFunction(validCahracters);
        for (String key : mVerbs.keySet()) {
            java.util.ArrayList<valueValidationFunction> curList = new java.util.ArrayList<valueValidationFunction>();
            curList.add(charValidation);
            mValidationDict.put(key, curList);
        }
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }
}
