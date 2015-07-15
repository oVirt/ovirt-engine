package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.ProviderType;

public class ProviderConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String URL = "URL";

    public ProviderConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(NAME);
        verbs.add(TYPE);
        verbs.add(DESCRIPTION);
        verbs.add(URL);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(TYPE, ProviderType.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(URL, String.class);

        // building the ColumnName dict.
        columnNameDict.put(NAME, "name");
        columnNameDict.put(TYPE, "provider_type");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(URL, "url");

        // Building the validation dict.
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(final String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        if (TYPE.equals(fieldName)) {
            return new EnumNameAutoCompleter(ProviderType.class);
        }
        return null;
    }

}
