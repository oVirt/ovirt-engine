package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PostgresSqlInjectionChecker extends SqlInjectionChecker {

    private static final Set<String> POSTGRES_SQL_COMMENT_INDICATORS =  new HashSet<>(Arrays.asList("--", "/*", "*/"));
    private static final Set<String> POSTGRES_SQL_FUNCTIONS = new HashSet<>(Arrays.asList(
            "ascii(", "chr(", "convert(", "cast(", "pg_sleep(", "length(", "substr(", "pg_read_file(", "replace(", " copy "));

    @Override
    protected String getSqlCommandSeperator() {
        return ";";
    }

    @Override
    protected String getSqlConcat() {
        return "||";
    }

    @Override
    protected Set<String> getCommentExpressions() {
        return POSTGRES_SQL_COMMENT_INDICATORS;
    }

    @Override
    protected Set<String> getInjectionFunctions() {
        return POSTGRES_SQL_FUNCTIONS;
    }
}
