package org.ovirt.engine.core.compat;

public class IntegerCompat {

    public static boolean TryParse(String value, RefObject<Integer> intRef) {
        boolean returnValue = false;
        try {
            intRef.argvalue = Integer.parseInt(value);
            returnValue = true;
        } catch (NumberFormatException e) {
            // eat it, return false
        }

        return returnValue;
    }

    public static boolean TryParse(String value, NumberStyles integer, CultureInfo currentCulture,
            RefObject<Integer> tempRefObject) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
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
