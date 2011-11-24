package org.ovirt.engine.core.utils;

import java.security.SecureRandom;

import org.ovirt.engine.core.compat.Encoding;

public final class Ticketing {
    public static String GenerateOTP() {
        SecureRandom secr = new SecureRandom();
        byte[] arrRandom = new byte[9];
        secr.nextBytes(arrRandom);
        // encode password into Base64 text:
        return Encoding.Base64.getString(arrRandom);
    }
}
