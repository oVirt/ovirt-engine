package org.ovirt.engine.core.compat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GWT Override for StringFormat
 */
public final class StringFormat {

    private static final Logger log = LoggerFactory.getLogger(StringFormat.class);

    /**
     * Format string using Java String.format() syntax (see {@link String#format(String, Object...)}) using a port of
     * java.util.Formatter
     */
    public static String format(String pattern, Object... args) {
        String message = new FormatterJava().format(pattern, args).toString();
        log.debug("Formatting Java pattern: '{}' With result: {}", pattern, message);
        return message;
    }

    /**
     * Format string using DotNet string.Format() syntax (using {0} references)
     */
    public static String formatDotNet(String pattern, Object... args) {
        return new FormatterDotnet().format(pattern, args).toString();
    }

}
