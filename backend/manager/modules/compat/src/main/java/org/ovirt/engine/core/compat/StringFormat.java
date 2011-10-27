package org.ovirt.engine.core.compat;

public final class StringFormat {
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }
}
