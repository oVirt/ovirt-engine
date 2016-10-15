package org.ovirt.engine.core.compat;

/**
 * Copied from .NET enum: System.Text.RegularExpressions.RegexOptions
 * Provides enumerated values to use to set regular expression options.
 */
public class RegexOptions {

    /**
     * Specifies that no options are set.
     */
    public static final int None = 0;

    /**
     * Specifies case-insensitive matching.
     */
    public static final int IgnoreCase = 1;

    /**
     * Multiline mode. Changes the meaning of ^ and $ so they match at the beginning and end, respectively, of any line,
     * and not just the beginning and end of the entire string.
     */
    public static final int Multiline = 2;

    /**
     * Specifies that the only valid captures are explicitly named or numbered groups of the form (?<i>name</i>...). This
     * allows unnamed parentheses to act as noncapturing groups without the syntactic clumsiness of the expression
     * (?:...).
     */
    public static final int ExplicitCapture = 4;

    /**
     * Specifies that the regular expression is compiled to an assembly. This yields faster execution but increases
     * startup time.
     */
    public static final int Compiled = 8;

    /**
     * Specifies single-line mode. Changes the meaning of the dot (.) so it matches every character (instead of every
     * character except \n).
     */
    public static final int Singleline = 16;

    /**
     * Eliminates unescaped white space from the pattern and enables comments marked with #. However, it
     * does not affect or eliminate white space in character classes.
     */
    public static final int IgnorePatternWhitespace = 32;

    /**
     * Specifies that the search will be from right to left instead of from left to right.
     */
    public static final int RightToLeft = 64;

    /**
     * Enables ECMAScript-compliant behavior for the expression. This value can be used only in conjunction with the
     * {@link #IgnoreCase}, {@link #Multiline} and {@link #Compiled} values. The use of this value with any other values
     * results in an exception.
     */
    public static final int ECMAScript = 256;

    /**
     * Specifies that cultural differences in language is ignored.
     */
    public static final int CultureInvariant = 512;
}
