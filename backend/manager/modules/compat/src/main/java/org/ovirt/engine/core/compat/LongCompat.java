package org.ovirt.engine.core.compat;

/**
 * @deprecated Use org.apache.commons.lang.math.NumberUtils instead.
 */
@Deprecated
public class LongCompat {

    public static Long tryParse(String value) {
        try {
            return new Long(value);
        } catch (NumberFormatException e) {
            // eat it and return null
            return null;
        }
    }

    public static long parseLong(String parseRangePart, NumberStyles style) {
        int radix = (style == NumberStyles.HexNumber) ? 16 : 10;
        return Long.parseLong(parseRangePart, radix);
    }

}
