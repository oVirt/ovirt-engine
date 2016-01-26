package org.ovirt.engine.core.searchbackend;

public class TimeConditionRelationAutoCompleter extends BaseAutoCompleter {

    public static final TimeConditionRelationAutoCompleter INSTANCE = new TimeConditionRelationAutoCompleter();

    private TimeConditionRelationAutoCompleter() {
        super( ">", "<", "=" );
    }
}
