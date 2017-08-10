package org.ovirt.engine.core.compat;

public final class StringHelper {
    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'IsNullOrEmpty'.
    // ------------------------------------------------------------------------------------
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotNullOrEmpty(String string) {
        return !isNullOrEmpty(string);
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

    public static String nullSafeJoin(String delimiter, Iterable<String> elements, String nullValue) {
        // delimiter being null is an error and we should throw exception
        // elements being null is acceptable and we should return nullValue
        return (elements != null) ? String.join(delimiter, elements) : nullValue;
    }
    public static String nullSafeJoin(String delimiter, Iterable<String> elements) {
        return nullSafeJoin(delimiter, elements, "");
    }
}
