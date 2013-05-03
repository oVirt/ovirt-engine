package org.ovirt.engine.core.searchbackend;

public class BiggerOrSmallerRelationAutoCompleter extends BaseAutoCompleter {

    public static final BiggerOrSmallerRelationAutoCompleter INSTANCE = new BiggerOrSmallerRelationAutoCompleter();

    private BiggerOrSmallerRelationAutoCompleter() {
        super("<", ">");
    }
}
