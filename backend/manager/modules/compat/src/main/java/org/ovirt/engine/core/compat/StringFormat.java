package org.ovirt.engine.core.compat;


/**
 * @deprecated Use {@link String#format(String, Object...)} instead. Only when not in a code that compiles to GWT
 *             (common, searchbackend etc..), in GWT compile time StringFormat is replaced with the StringFormat class
 *             in gwt-extension ui overrides which uses a GWT String formatter since GWT String wrapper does not
 *             contains a format method
 */
@Deprecated
public final class StringFormat {
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }
}
