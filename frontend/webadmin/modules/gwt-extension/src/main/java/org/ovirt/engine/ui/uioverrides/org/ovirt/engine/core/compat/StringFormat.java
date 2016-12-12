package org.ovirt.engine.core.compat;

import java.util.logging.Logger;

/**
 * GWT Override for StringFormat
 */
public final class StringFormat {

    private static final Logger log = Logger.getLogger(StringFormat.class.getName());

    /**
     * Format string using Java String.format() syntax (see {@link String#format(String, Object...)}) using a port of
     * java.util.Formatter
     */
    public static String format(String pattern, Object... args) {
        String message = new FormatterJava().format(pattern, args).toString();
        log.fine("Formatting Java pattern: '" + pattern + "' with result: " + message);
        return message;
    }

}
