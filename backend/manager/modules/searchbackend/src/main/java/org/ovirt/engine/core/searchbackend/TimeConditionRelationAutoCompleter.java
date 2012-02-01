package org.ovirt.engine.core.searchbackend;

public class TimeConditionRelationAutoCompleter extends BaseAutoCompleter {

    public final static TimeConditionRelationAutoCompleter INSTANCE = new TimeConditionRelationAutoCompleter();

    private TimeConditionRelationAutoCompleter() {
        super(new String[] { ">", "<", "=" });
    }
}
