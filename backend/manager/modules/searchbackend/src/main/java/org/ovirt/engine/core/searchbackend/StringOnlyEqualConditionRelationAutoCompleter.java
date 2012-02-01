package org.ovirt.engine.core.searchbackend;

public final class StringOnlyEqualConditionRelationAutoCompleter extends BaseAutoCompleter {

    public final static StringOnlyEqualConditionRelationAutoCompleter INSTANCE =
            new StringOnlyEqualConditionRelationAutoCompleter();

    private StringOnlyEqualConditionRelationAutoCompleter() {
        super("=");
    }
}
