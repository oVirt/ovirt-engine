package org.ovirt.engine.core.compat;

/**
 * @deprecated Use org.apache.commons.lang.math.NumberUtils instead.
 */
@Deprecated
public class IntegerCompat {

    /**
     * Try parse an integer, return null if failed.
     * @param value     the string format of the number
     * @return          Integer or null
     */
    public static Integer tryParse(final String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            // eat it, return null
            return null;
        }
    }

}
