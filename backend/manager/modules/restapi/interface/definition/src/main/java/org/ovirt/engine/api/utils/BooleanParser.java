package org.ovirt.engine.api.utils;

/**
 * This class contains methods that parse boolean from strings accepting only the values {@code true} and {@code false}.
 */
public class BooleanParser {
    public static boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new InvalidValueException(
            "Value \"" + value + "\" isn't a valid boolean, it should be \"true\" or \"false\""
        );
    }
}
