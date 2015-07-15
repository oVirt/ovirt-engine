package org.ovirt.engine.core.searchbackend;

public class VdcGroupConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String GRPNAME = "GRPNAME";
    public static final String NAME = "NAME";

    public VdcGroupConditionFieldAutoCompleter() {
        super();

        // Building the basic verbs dictionary:
        verbs.add(GRPNAME);
        verbs.add(NAME);

        // Building the auto completion dictionary:
        buildCompletions();

        // Building the types dictionary:
        getTypeDictionary().put(GRPNAME, String.class);
        getTypeDictionary().put(NAME, String.class);

        // building the column name dictionary:
        columnNameDict.put(GRPNAME, "name");
        columnNameDict.put(NAME, "name");

        // Building the validation dictionary:
        buildBasicValidationTable();
    }


    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

}
