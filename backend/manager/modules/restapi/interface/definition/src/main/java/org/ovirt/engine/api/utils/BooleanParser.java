package org.ovirt.engine.api.utils;

/**
 * This class contains methods that parse boolean from strings accepting only the values {@code true}, {@code false},
 * {@code 0} and {@code 1} as described in <a href="http://www.w3.org/TR/xmlschema-2/#boolean">section 3.2.2.1</a> of
 * the XML schema specification.
 */
public class BooleanParser {
    public static boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }
        throw new InvalidValueException(
            "Value \"" + value + "\" isn't a valid boolean, it should be \"true\" , \"false\", \"0\" or \"1\""
        );
    }
}
