package org.ovirt.engine.core.compat;

/**
 * @deprecated Please use #org.apache.commons.lang.StringUtils and avoid further usage of this class.
 */
@Deprecated
public final class StringHelper {
    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'IsNullOrEmpty'.
    // ------------------------------------------------------------------------------------
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Join' (2 parameter
    // version).
    // ------------------------------------------------------------------------------------
    public static String join(String separator, Object[] values) {
        if (values == null) {
            return null;
        } else {
            return join(separator, values, 0, values.length);
        }
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Join' (4 parameter
    // version).
    // ------------------------------------------------------------------------------------
    public static String join(final String separator, final Object[] objectArray, final int startindex, final int count) {
        //pre-allocate some space for better concatenation speed
        final StringBuilder result = new StringBuilder((count - startindex) * 32);

        if (objectArray == null) {
            return null;
        }

        for (int index = startindex; index < objectArray.length && index - startindex < count; index++) {
            if (separator != null && index > startindex) {
                result.append(separator);
            }

            if (objectArray[index] != null) {
                result.append(objectArray[index].toString());
            }
        }

        return result.toString();
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'TrimEnd'.
    // ------------------------------------------------------------------------------------
    public static String trimEnd(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null) {
            return string;
        }

        int lengthToKeep = string.length();
        for (int index = string.length() - 1; index >= 0; index--) {
            boolean removeChar = false;
            if (charsToTrim.length == 0) {
                if (Character.isSpace(string.charAt(index))) {
                    lengthToKeep = index;
                    removeChar = true;
                }
            } else {
                for (int trimCharIndex = 0; trimCharIndex < charsToTrim.length; trimCharIndex++) {
                    if (string.charAt(index) == charsToTrim[trimCharIndex]) {
                        lengthToKeep = index;
                        removeChar = true;
                        break;
                    }
                }
            }
            if (!removeChar) {
                break;
            }
        }
        return string.substring(0, lengthToKeep);
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'TrimStart'.
    // ------------------------------------------------------------------------------------
    public static String trimStart(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null) {
            return string;
        }

        int startingIndex = 0;
        for (int index = 0; index < string.length(); index++) {
            boolean removeChar = false;
            if (charsToTrim.length == 0) {
                if (Character.isSpace(string.charAt(index))) {
                    startingIndex = index + 1;
                    removeChar = true;
                }
            } else {
                for (int trimCharIndex = 0; trimCharIndex < charsToTrim.length; trimCharIndex++) {
                    if (string.charAt(index) == charsToTrim[trimCharIndex]) {
                        startingIndex = index + 1;
                        removeChar = true;
                        break;
                    }
                }
            }
            if (!removeChar) {
                break;
            }
        }
        return string.substring(startingIndex);
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Trim' when arguments
    // are used.
    // ------------------------------------------------------------------------------------
    public static String trim(String string, Character... charsToTrim) {
        return trimEnd(trimStart(string, charsToTrim), charsToTrim);
    }

    public static String trim(String s, char[] cs) {
        Character[] chars = new Character[cs.length];
        for (int i = 0; i < cs.length; i++) {
            chars[i] = cs[i];
        }

        return trim(s, chars);
    }

    public static boolean stringsEqualIgnoreCase(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else {
            return s1 != null && s1.equalsIgnoreCase(s2);
        }
    }

    /**
     * Returns a literal pattern <code>String</code> for the specified <code>String</code>.
     *
     * <p>
     * This method produces a <code>String</code> that can be used to create a <code>Pattern</code> that would match the
     * string <code>s</code> as if it were a literal pattern.
     * </p>
     * Metacharacters or escape sequences in the input sequence will be given no special meaning.
     *
     * Copied from Pattern.java code for GWT compatibility.
     *
     * @param s
     *            The string to be literalized
     * @return A literal string replacement
     * @since 1.5
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1) {
            return "\\Q" + s + "\\E";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

    /**
     * Replaces a null String with "", otherwise returns the original String.
     */
    public static String defaultString(String s) {
        return s == null ? "" : s;
    }

}
