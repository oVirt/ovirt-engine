package org.ovirt.engine.core.compat;

import java.math.BigDecimal;

public class DoubleCompat {

    // public static boolean TryParse(String value, BigDecimal test) {
    // throw new
    // NotImplementedException("See the TryParse which takes a RefObject");
    // }

    public static boolean TryParse(String value, RefObject<BigDecimal> refDec) {
        boolean returnValue = false;
        try {
            refDec.argvalue = new BigDecimal(value);
            returnValue = true;
        } catch (NumberFormatException e) {
            // eat it and return
        }
        return returnValue;
    }

    public static boolean TryParse2(String value, RefObject<Double> refDec) {
        boolean returnValue = false;
        try {
            refDec.argvalue = new Double(value);
            returnValue = true;
        } catch (NumberFormatException e) {
            // eat it and return
        }
        return returnValue;
    }

}
