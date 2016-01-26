package org.ovirt.engine.core.searchbackend;

public class NumericConditionRelationAutoCompleter extends BaseAutoCompleter {

    public static final NumericConditionRelationAutoCompleter INSTANCE = new NumericConditionRelationAutoCompleter();

    private NumericConditionRelationAutoCompleter() {
        super("<", ">", "<=", ">=", "=", "!=");
    }
}
