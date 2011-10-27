package org.ovirt.engine.core.searchbackend;

public class StringConditionRelationAutoCompleter extends BaseAutoCompleter {
    public StringConditionRelationAutoCompleter() {
        super(new String[] { "=", "!=" });

    }
}
