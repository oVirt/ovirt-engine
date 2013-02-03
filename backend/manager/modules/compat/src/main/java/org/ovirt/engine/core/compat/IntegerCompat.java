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

    // public static boolean TryParse(String value, int test) {
    // throw new
    // NotImplementedException("See the try parse which takes in a refobject");
    // }

    /**
     * Compare two integers safely even if one or both are nulls
     */
    public static boolean equalsWithNulls(Integer i1, Integer i2) {
        if (i1 == null && i2 == null)
            return true; // both nulls
        if (i1 == null || i2 == null)
            return false; // one is null the other is not
        return i1.equals(i2); // both are not nulls
    }

}
