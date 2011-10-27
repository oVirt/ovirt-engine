package org.ovirt.engine.core.compat;

import java.nio.charset.Charset;

public class Encoding {
    public final static Encoding ASCII = new Encoding("US-ASCII");
    public final static Encoding UTF8 = new Encoding("UTF-8");
    public final static Encoding Unicode = new Encoding("UTF-16");

    private Charset charset;

    public Encoding(String encoding) {
        charset = Charset.forName(encoding);
    }

    public byte[] GetBytes(String plainText) {
        return plainText.getBytes(charset);
    }

    public String GetString(byte[] decrypt) {
        try {
            return new String(decrypt, charset.displayName());
        } catch (Exception e) {
            throw new CompatException(e);
        }
    }
}
