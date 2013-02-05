package org.ovirt.engine.core.compat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is a wrapper for the java.util.regex.Pattern class
/**
 * @deprecated Use {@link Pattern} instead, see usage exaple at:
 * http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
 */
@Deprecated
public class Regex {
    public static Match Match(String string, String pattern) {
        return Match(string, pattern, 0);
    }

    public static Match Match(String string, String pattern, int options) {
        Matcher matcher = Pattern.compile(pattern, options).matcher(string);
        boolean success = matcher.find();
        return new Match(matcher.toMatchResult(), success);
    }

    private Pattern pattern;

    public Regex(String string) {
        pattern = Pattern.compile(string);
    }

    public Regex(String string, int options) {
        pattern = Pattern.compile(string, options);
    }

    public boolean IsMatch(String candidate) {
        return pattern.matcher(candidate).find();
    }

    public static boolean IsMatch(String candidate, String regEx) {
        return (new Regex(regEx)).IsMatch(candidate);
    }

    public static String replace(String searchString, String pageStringRegex, String format) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }

    public String replace(String searchClause, String string) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }

    public static boolean IsMatch(String string, String expression, RegexOptions options) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }

    public static boolean IsMatch(String searchString, String pageStringRegex, int ignorecase) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }
}
