package org.ovirt.engine.core.searchbackend;

public class NumericConditionRelationAutoCompleter extends BaseAutoCompleter {
    public NumericConditionRelationAutoCompleter() {
        super(new String[] { "<", ">", "<=", ">=", "=", "!=" });

    }
}
