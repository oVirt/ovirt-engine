package org.ovirt.engine.core.searchbackend;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.ovirt.engine.core.common.errors.SearchEngineIllegalCharacterException;
import org.ovirt.engine.core.compat.StringFormat;


public abstract class SqlInjectionChecker {
    private static final char QUOTE = '\'';
    private static final char BACKSLASH = '\\';
    private static final char PERCENT = '%';
    private static final char BLANK = ' ';

    private static final String BACKSLASH_STR = "\\";
    private static final String QUOTE_STR = "'";
    private static final String DOUBLE_QUOTE_STR = "\"";
    private static final String BACKSLASH_QUOTE = "\\\\'";
    private static final String BACKSLASH_DOUBLE_QUOTE = "\\\\\"";
    private static final String QUOTE_QUOTE = "''";
    private static final String DELIMITERS = "'\"";
    private HashSet<String> sqlInjectionExpressions = new HashSet<>();
    private static final String[] ANSI_SQL_KEYWORDS = { " insert ", " delete ", " update ", " create ", " drop  ", " union ", " alter ",
                                                 " if ", " else ", "sum(", "min(", "max(", "count(", "avg(", " having "};
    SqlInjectionChecker() {
        for (String s : ANSI_SQL_KEYWORDS) {
            addInjectionExpression(s);
        }
        sqlInjectionExpressions.add(getSqlCommandSeperator());
        sqlInjectionExpressions.add(getSqlConcat());
        sqlInjectionExpressions.addAll(getCommentExpressions());
        sqlInjectionExpressions.addAll(getInjectionFunctions());
    }
    /**
     * Adds an entry to injection expressions.
     * @param Expr the expression.
     */
    public void addInjectionExpression(String Expr){
        sqlInjectionExpressions.add(Expr);
    }
    /**
     * Checks if the given sql has SQL Injection expressions
     * @param sql the sql string
     * @return boolean
     */
    public boolean hasSqlInjection(String sql) {
        sql = removeAllStringValuesFromSql(sql);
        if (sql.length() > 0) {
            // replace all functions to have the format "f(" in order to match it exactly.
            sql = sql.replaceAll("\\s+\\(", "(");
            // look for sql injection expressions
            for (String expr : sqlInjectionExpressions) {
                if (sql.contains(expr)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Removes all values enclosed by single or double quotes from original sql
     * in order to test for injection only on sql keywords and not on values.
     */
    private String removeAllStringValuesFromSql(String sql) {
        boolean singleQuoteFound=false;
        boolean doubleQuoteFound=false;
        StringBuilder sb = new StringBuilder();
        // replace all occurrences of a quote/s inside a value with an empty string.
        final String[] QUOTES_INSIDE_VALUE_INDICATORES = {BACKSLASH_QUOTE, QUOTE_QUOTE, BACKSLASH_DOUBLE_QUOTE};
        for (String s : QUOTES_INSIDE_VALUE_INDICATORES) {
            sql = sql.replaceAll(s, "");
        }
        StringTokenizer st = new StringTokenizer(sql, DELIMITERS, true);
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(QUOTE_STR)) {
                if(singleQuoteFound){
                singleQuoteFound = false; // closing '
                continue;
                }
                else if (!doubleQuoteFound){ // ignore single quote inside double quotes
                    singleQuoteFound = true; // opening '
                    continue;
                }
            }
            else if (token.equals(DOUBLE_QUOTE_STR)) {
                if(doubleQuoteFound){
                    doubleQuoteFound = false; // closing ""
                    continue;
                }
                else if (!singleQuoteFound){ // ignore double quote inside single quotes
                    doubleQuoteFound = true; // opening "
                    continue;
                }
            }
            if (!singleQuoteFound && !doubleQuoteFound) {
                sb.append(token);
            }
        }
        return sb.toString();
    }
    /**
     * Enforce escaping special characters in an expression by proceeding them with a backslash.
     * @param value the expression value
     * @return String the formatted expression.
     */
    public static String  enforceEscapeCharacters(String value) {
        StringBuilder sb = new StringBuilder();
        if (value.indexOf(QUOTE) >= 0 || value.indexOf(BACKSLASH) >= 0 || value.indexOf(PERCENT) >= 0) {
            // the following is a Postgres limitation, since we are using LIKE/ILIKE and
            // Postgres does not allow that last value character is the default ESCAPE ('\')
            if (value.endsWith(BACKSLASH_STR)) {
                throw new SearchEngineIllegalCharacterException();
            }
            char[] sourceArray = value.toCharArray();
            int i = 0;
            char prev = BLANK;
            char next = BLANK;
            for (Character c : sourceArray) {
                switch (c) {
                case QUOTE:
                case PERCENT: // Those values should be formatted as \' or \%
                    if (i > 0 && prev == BACKSLASH) {
                        sb.append(c);
                    } else {
                        sb.append(BACKSLASH);
                        sb.append(c);
                    }
                    break;
                case BACKSLASH: // A backslash should be formatted as \\
                    if ((i > 0 && prev == BACKSLASH) || (next == QUOTE || next == PERCENT || next == BACKSLASH)) {
                        sb.append(c);
                    } else {
                        sb.append(BACKSLASH);
                        sb.append(BACKSLASH);
                    }
                    break;
                default: // regular , copy character as is
                    sb.append(c);
                    break;
                }
                prev = c.charValue();
                i++;
                next = (i < sourceArray.length - 1) ? sourceArray[i + 1] : BLANK;
            }
        } else {
            sb.append(value);
        }
        return StringFormat.format("'%1$s'", sb.toString());
    }
    /**
     * gets the database vendor specific sql command separator
     */
    protected abstract String getSqlCommandSeperator();
    /**
     * gets the database vendor specific sql string concatenation
     */
    protected abstract String getSqlConcat();
    /**
     * gets the database vendor specific sql comment begin/end definition
     */
    protected abstract HashSet<String> getCommentExpressions();
    /**
     * gets the database vendor specific functions that are considered as sql injection.
     */
    protected abstract HashSet<String> getInjectionFunctions();
}
