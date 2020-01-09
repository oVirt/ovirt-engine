package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class ClusterConditionFieldAutoCompleter extends BaseConditionFieldAutoCompleter {
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String COMMENT = "COMMENT";
    public static final String ARCHITECTURE = "ARCHITECTURE";
    public static final String COMPATIBILITY_LEVEL = "COMPATIBILITY_LEVEL";
    public static final String CPU_TYPE = "CPU_TYPE";

    public ClusterConditionFieldAutoCompleter() {
        // Building the basic vervs Dict
        verbs.add(NAME);
        verbs.add(DESCRIPTION);
        verbs.add(COMMENT);
        verbs.add(ARCHITECTURE);
        verbs.add(COMPATIBILITY_LEVEL);
        verbs.add(CPU_TYPE);

        // Building the autoCompletion Dict
        buildCompletions();
        // Building the types dict
        getTypeDictionary().put(NAME, String.class);
        getTypeDictionary().put(DESCRIPTION, String.class);
        getTypeDictionary().put(COMMENT, String.class);
        getTypeDictionary().put(ARCHITECTURE, ArchitectureType.class);
        getTypeDictionary().put(COMPATIBILITY_LEVEL, Float.class);
        getTypeDictionary().put(CPU_TYPE, String.class);

        // building the ColumnName Dict
        columnNameDict.put(NAME, "name");
        columnNameDict.put(DESCRIPTION, "description");
        columnNameDict.put(COMMENT, "free_text_comment");
        columnNameDict.put(ARCHITECTURE, "architecture");
        columnNameDict.put(COMPATIBILITY_LEVEL, "compatibility_version");
        columnNameDict.put(CPU_TYPE, "cpu_name");

        // Building the validation dict
        buildBasicValidationTable();
    }

    @Override
    public IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName) {
        return StringConditionRelationAutoCompleter.INSTANCE;
    }

    @Override
    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName) {
        IConditionValueAutoCompleter retval = null;
        if (ARCHITECTURE.equals(fieldName)) {
            retval = new EnumValueAutoCompleter(ArchitectureType.class);
        }
        return retval;
    }
}
