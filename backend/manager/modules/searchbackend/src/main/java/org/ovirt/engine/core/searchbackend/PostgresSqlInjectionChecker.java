package org.ovirt.engine.core.searchbackend;

import java.util.HashSet;

public class PostgresSqlInjectionChecker extends SqlInjectionChecker {

    private static final String[] POSTGRES_SQL_COMMENT_INDICATORS = {"--", "/*", "*/"};
    private static final String[] POSTGRES_SQL_FUNCTIONS = {"ascii(", "chr(", "convert(", "cast(", "pg_sleep(", "length(", "substr(", "pg_read_file(", "replace(", " copy "};

    @Override
    protected String getSqlCommandSeperator() {
        return ";";
    }

    @Override
    protected String getSqlConcat() {
        return "||";
    }

    @Override
    protected HashSet<String> getCommentExpressions() {
        HashSet<String> commentExpressions = new HashSet<>();
        for (String s : POSTGRES_SQL_COMMENT_INDICATORS) {
            commentExpressions.add(s);
        }
        return commentExpressions;
    }

    @Override
    protected HashSet<String> getInjectionFunctions() {
        HashSet<String> functionExpressions = new HashSet<>();
        for (String s : POSTGRES_SQL_FUNCTIONS) {
            functionExpressions.add(s);
        }
        return functionExpressions;
    }

}
