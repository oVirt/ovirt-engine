package org.ovirt.engine.core.compat;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

public enum Encoding {
    ASCII("US-ASCII"),
    UTF8("UTF-8"),
    Unicode("UTF-16"),
    Base64("Base64");

    private Charset charset;
    private Base64 codec;

    private Encoding(String encoding) {
        if ("Base64".equals(encoding)) {
            codec = new Base64();
        } else {
            charset = Charset.forName(encoding);
        }
    }

    public byte[] getBytes(String plainText) {
        if (this != Base64) {
            return plainText.getBytes(charset);
        } else {
            try {
                return codec.decode(plainText.getBytes("US-ASCII"));
            } catch (Exception e) {
                throw new CompatException(e);
            }
        }
    }

    public String getString(byte[] decrypt) {
        if (this != Base64) {
            try {
                return new String(decrypt, charset.displayName());
            } catch (Exception e) {
                throw new CompatException(e);
            }
        } else {
            return new String(codec.encode(decrypt)).trim();
        }
    }
}
