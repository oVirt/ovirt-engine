package org.ovirt.engine.core.compat;

public class BooleanCompat {

    public static boolean TryParse(String value, RefObject<Boolean> boolRef) {
        boolean returnValue = false;
        try {
            boolRef.argvalue = Boolean.parseBoolean(value);
            returnValue = true;
        } catch (NumberFormatException e) {
            // parse failed
            return false;
        }

        return returnValue;
    }

}
