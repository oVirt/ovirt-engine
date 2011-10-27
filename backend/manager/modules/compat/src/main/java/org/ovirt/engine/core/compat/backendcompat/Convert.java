package org.ovirt.engine.core.compat.backendcompat;

import org.apache.commons.codec.binary.Base64;

import org.ovirt.engine.core.compat.CompatException;

public class Convert {
    public static byte[] FromBase64String(String source) {
        Base64 codec = new Base64();
        try {
            return codec.decode(source.getBytes("US-ASCII"));
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }

    public static String ToBase64String(byte[] cipher) {
        Base64 codec = new Base64();
        return new String(codec.encode(cipher)).trim();
    }

    public static String ToBase64String(String encrypt) {
        Base64 codec = new Base64();
        try {
            return new String(codec.encode(encrypt.getBytes("US-ASCII"))).trim();
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }

}
