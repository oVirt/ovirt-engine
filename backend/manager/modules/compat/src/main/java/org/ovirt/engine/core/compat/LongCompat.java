package org.ovirt.engine.core.compat;

public class LongCompat {

    /**
     * Convert a String to a Long.
     *
     * Returns null if conversion is unsuccessful.
     * @param value
     *            the string format of the number
     * @return Long on success or null on failure
     */
    public static Long tryParse(final String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
