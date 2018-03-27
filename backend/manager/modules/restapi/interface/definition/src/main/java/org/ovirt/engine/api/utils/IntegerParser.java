package org.ovirt.engine.api.utils;

import java.math.BigInteger;

import javax.xml.bind.DatatypeConverter;

/**
 * This class contains methods that parse integers from strings checking and avoiding overflows of the corresponding
 * Java integer types.
 */
public class IntegerParser {
    // Max values of Java integer types:
    private static final BigInteger MAX_SHORT = new BigInteger("32767"); // 2^(16-1)-1
    private static final BigInteger MAX_UNSIGNED_SHORT = new BigInteger("65535"); // 2^16-1
    private static final BigInteger MAX_INT = new BigInteger("2147483647"); // 2^(32-1)-1
    private static final BigInteger MAX_UNSIGNED_INT = new BigInteger("4294967295"); // 2^32-1
    private static final BigInteger MAX_LONG = new BigInteger("9223372036854775807"); // 2^(64-1)-1

    public static short parseShort(String value) {
        if (value.trim().equals("")) {
            value = "0";
        }
        BigInteger result = DatatypeConverter.parseInteger(value);
        if (result.compareTo(MAX_SHORT) > 0) {
            throw new InvalidValueException("Value " + value + " is greater than the maximum short " + MAX_SHORT);
        }
        return result.shortValue();
    }

    public static int parseUnsignedShort(String value) {
        if (value.trim().equals("")) {
            value = "0";
        }
        BigInteger result = DatatypeConverter.parseInteger(value);
        if (result.compareTo(MAX_UNSIGNED_SHORT) > 0) {
            throw new InvalidValueException("Value " + value + " is greater than maximum unsigned short " + MAX_UNSIGNED_SHORT);
        }
        if (result.intValue() < 0) {
            throw new InvalidValueException("Negative value " + value +  " not allowed for unsigned shorts, valid values are between 0 and " + MAX_UNSIGNED_SHORT);
        }
        return result.intValue();
    }

    public static int parseInt(String value) {
        if (value.trim().equals("")) {
            value = "0";
        }
        BigInteger result = DatatypeConverter.parseInteger(value);
        if (result.compareTo(MAX_INT) > 0) {
            throw new InvalidValueException("Value " + value +  " is greater than maximum integer " + MAX_INT);
        }
        return result.intValue();
    }

    public static long parseUnsignedInt(String value) {
        if (value.trim().equals("")) {
            value = "0";
        }
        BigInteger result = DatatypeConverter.parseInteger(value);
        if (result.compareTo(MAX_UNSIGNED_INT) > 0) {
            throw new InvalidValueException("Value " + value +  " is greater than maximum unsigned integer " + MAX_UNSIGNED_INT);
        }
        if (result.intValue() < 0) {
            throw new InvalidValueException("Negative value " + value +  " not allowed for unsigned integers, valid values are between 0 and " + MAX_UNSIGNED_INT);
        }
        return result.longValue();
    }

    public static long parseLong(String value) {
        if (value.trim().equals("")) {
            value = "0";
        }
        BigInteger result = DatatypeConverter.parseInteger(value);
        if (result.compareTo(MAX_LONG) > 0) {
            throw new InvalidValueException("Value " + value +  " is greater than maximum long " + MAX_LONG);
        }
        return result.longValue();
    }
}
