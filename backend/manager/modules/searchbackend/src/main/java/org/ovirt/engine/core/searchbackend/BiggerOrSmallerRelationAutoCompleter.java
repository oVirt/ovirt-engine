package org.ovirt.engine.core.searchbackend;

public class BiggerOrSmallerRelationAutoCompleter extends BaseAutoCompleter {

    public static final BiggerOrSmallerRelationAutoCompleter INTSANCE = new BiggerOrSmallerRelationAutoCompleter();

    private BiggerOrSmallerRelationAutoCompleter() {
        super(new String[] { "<", ">" });
    }
}
