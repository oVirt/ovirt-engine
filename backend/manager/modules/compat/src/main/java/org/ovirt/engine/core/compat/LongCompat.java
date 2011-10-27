package org.ovirt.engine.core.compat;

public class LongCompat {

    public static boolean TryParse(String value, RefObject<Long> refDec) {
        boolean returnValue = false;
        try {
            refDec.argvalue = new Long(value);
            returnValue = true;
        } catch (NumberFormatException e) {
            // eat it and return
        }
        return returnValue;
    }

    public static long parseLong(String parseRangePart, NumberStyles style) {
        int radix = (style == NumberStyles.HexNumber) ? 16 : 10;
        return Long.parseLong(parseRangePart, radix);
    }

}
