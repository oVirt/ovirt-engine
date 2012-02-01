package org.ovirt.engine.core.searchbackend;

public class NumericConditionRelationAutoCompleter extends BaseAutoCompleter {

    public final static NumericConditionRelationAutoCompleter INSTANCE = new NumericConditionRelationAutoCompleter();

    private NumericConditionRelationAutoCompleter() {
        super(new String[] { "<", ">", "<=", ">=", "=", "!=" });
    }
}
