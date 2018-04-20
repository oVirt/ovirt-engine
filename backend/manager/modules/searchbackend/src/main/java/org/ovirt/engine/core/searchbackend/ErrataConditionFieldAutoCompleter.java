package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;

public class ErrataConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {

    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    public static final String SEVERITY = "SEVERITY";

    public ErrataConditionFieldAutoCompleter() {
        // Building the basic verbs dict.
        verbs.add(NAME);
        verbs.add(TYPE);
        verbs.add(SEVERITY);

        // Building the autoCompletion dict.
        buildCompletions();

        // Building the types dict.
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(TYPE, ErrataType.class);
        getTypeDictionary().put(SEVERITY, ErrataSeverity.class);

        // building the ColumnName dict.
        columnNameDict.put(NAME, "name");
        columnNameDict.put(TYPE, "errata_type");
        columnNameDict.put(SEVERITY, "severity");

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
            return new EnumNameAutoCompleter(ErrataType.class);
        } else if (SEVERITY.equals(fieldName)) {
            return new EnumNameAutoCompleter(ErrataSeverity.class);
        }
        return null;
    }

}
