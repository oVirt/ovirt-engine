package org.ovirt.engine.core.compat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GWT Override for StringFormat
 */
public final class StringFormat {

    private static final Log log = LogFactory.getLog(StringFormat.class);

    /**
     * Format string using Java String.format() syntax (see {@link String#format(String, Object...)}) using a port of
     * java.util.Formatter
     */
    public static String format(String pattern, Object... args) {
        String message = new FormatterJava().format(pattern, args).toString();
        log.debugFormat("Formatting Java pattern: {0} With result: {1}", pattern, message);
        return message;
    }

    /**
     * Format string using DotNet string.Format() syntax (using {0} references)
     */
    public static String formatDotNet(String pattern, Object... args) {
        return new FormatterDotnet().format(pattern, args).toString();
    }

}
