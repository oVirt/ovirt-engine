package org.ovirt.engine.core.utils;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

public final class Ticketing {
    public static String generateOTP() {
        SecureRandom secr = new SecureRandom();
        byte[] arrRandom = new byte[9];
        secr.nextBytes(arrRandom);
        // encode password into Base64 text:
        return new Base64(0).encodeToString(arrRandom);
    }
}
