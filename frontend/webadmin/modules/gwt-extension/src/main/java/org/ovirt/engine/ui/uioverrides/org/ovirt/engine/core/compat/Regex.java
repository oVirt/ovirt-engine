package org.ovirt.engine.core.compat;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Replacement for System.Text.RegularExpressions.Regex
 */
public class Regex {

    public static boolean isMatch(String input, String pattern) {
        return isMatch(input, pattern, RegexOptions.None);
    }

    public static boolean isMatch(String input, String pattern, int options) {
        return new Regex(pattern, options).isMatch(input);
    }

    public static Match match(String input, String pattern) {
        return match(input, pattern, RegexOptions.None);
    }

    public static Match match(String input, String pattern, int options) {
        return new Regex(pattern, options).match(input);
    }

    public static String replace(String input, String pattern, String replacement) {
        return replace(input, pattern, replacement, RegexOptions.None);
    }

    public static String replace(String input, String pattern, String replacement, int options) {
        return new Regex(pattern, options).replace(input, replacement);
    }

    private final String pattern;

    private final RegExp impl;

    private final int options;

    public Regex(String pattern) {
        this(pattern, RegexOptions.None);
    }

    public Regex(String pattern, int options) {
        this.pattern = pattern;
        this.options = options;
        String flags = "";
        if (this.isGlobal()) {
            flags += 'g';
        }
        if (this.isIgnoreCase()) {
            flags += 'i';
        }
        if (this.isMultiLine()) {
            flags += 'm';
        }
        impl = RegExp.compile(pattern, flags);
    }

    public boolean isGlobal() {
        return (options & RegexOptions.Singleline) != 0;
    }

    public boolean isIgnoreCase() {
        return (options & RegexOptions.IgnoreCase) != 0;
    }

    public boolean isMatch(String input) {
        return impl.test(input);
    }

    public boolean isMultiLine() {
        return (options & RegexOptions.Multiline) != 0;
    }

    public Match match(String input) {
        MatchResult matchResult = impl.exec(input);
        return new Match(matchResult);
    }

    public String replace(String input, String replacement) {
        return impl.replace(input, replacement);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RegExp [pattern=");
        builder.append(pattern);
        builder.append(", isGlobal()=");
        builder.append(isGlobal());
        builder.append(", isIgnoreCase()=");
        builder.append(isIgnoreCase());
        builder.append(", isMultiLine()=");
        builder.append(isMultiLine());
        builder.append("]");
        return builder.toString();
    }

}
