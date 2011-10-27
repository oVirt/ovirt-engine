package org.ovirt.engine.core.compat.backendcompat;

import java.io.UnsupportedEncodingException;

public class UTF8EncodingCompat {

    public String GetString(byte[] byteArrayOvfData) {
        try {
            return new String(byteArrayOvfData, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported charset", e);
        }
    }

    public byte[] GetBytes(String strSysPrepContent) {
        try {
            return strSysPrepContent.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported charset", e);
        }
    }

}
