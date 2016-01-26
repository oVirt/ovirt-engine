package org.ovirt.engine.core.searchbackend;

public final class StringConditionRelationAutoCompleter extends BaseAutoCompleter {

    public static final StringConditionRelationAutoCompleter INSTANCE = new StringConditionRelationAutoCompleter();

    private StringConditionRelationAutoCompleter() {
        super("=", "!=");
    }
}
