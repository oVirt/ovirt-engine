package org.ovirt.engine.core.searchbackend;

import java.util.UUID;

public class JobConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    private static final String CORRELATION_ID = "CORRELATION_ID";

    public JobConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(CORRELATION_ID);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(CORRELATION_ID, UUID.class);

        // building the ColumnName dict.
        columnNameDict.put(CORRELATION_ID, "correlation_id");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        if (CORRELATION_ID.equals(fieldName)) {
            return StringConditionRelationAutoCompleter.INSTANCE;
        }
        return null;
    }
}
