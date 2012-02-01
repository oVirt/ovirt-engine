package org.ovirt.engine.core.searchbackend;

public final class StringConditionRelationAutoCompleter extends BaseAutoCompleter {

    public final static StringConditionRelationAutoCompleter INSTANCE = new StringConditionRelationAutoCompleter();

    private StringConditionRelationAutoCompleter() {
        super(new String[] { "=", "!=" });
    }
}
