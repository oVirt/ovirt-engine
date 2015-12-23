package org.ovirt.engine.core.common.utils;

import java.util.regex.Pattern;

public class ErrorMessageUtils {

    private static Pattern VARIABLE_DECLARATION_REGEX = Pattern.compile("^\\$[^{]");

    public static boolean isMessage(String message) {
        return !isVariableDeclaration(message);
    }

    public static boolean isVariableDeclaration(String message) {
        return VARIABLE_DECLARATION_REGEX.matcher(message).matches();
    }

}
