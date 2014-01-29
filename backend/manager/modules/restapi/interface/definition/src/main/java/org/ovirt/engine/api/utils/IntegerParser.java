package org.ovirt.engine.api.utils;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;

public class IntegerParser {

    public static int parseIntegerToShort(String value) {
        BigInteger result =
                DatatypeConverter.parseInteger(value);
        if (result.longValue() > Short.MAX_VALUE) {
            throw new InvalidValueException("Value " +value+ " greater than maximum " + Short.MAX_VALUE);
        }
        return result.intValue();
    }

    public static int parseIntegerToInt(String value) {
        BigInteger result =
                DatatypeConverter.parseInteger(value);
        if (result.longValue() > Integer.MAX_VALUE) {
            throw new InvalidValueException("Value " +value+ " greater than maximum " + Integer.MAX_VALUE);
        }
        return result.intValue();
    }

}
