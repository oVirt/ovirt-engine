package org.ovirt.engine.core.compat;


/**
 * @deprecated Use {@link String#format(String, Object...)} instead.
 */
@Deprecated
public final class StringFormat {
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }
}
