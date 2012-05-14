package org.ovirt.engine.core.engineencryptutils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EncryptionUtilsTest {

    @Test
    public void testDefaultEndoceDecode() {
        String secret = "i'm going to be encrypted";
        String encode = EncryptionUtils.encode(secret, null, null);
        assertNotNull(encode);
        String decode = EncryptionUtils.decode(encode, null, null);
        assertNotNull(encode);
        assertTrue(secret.equals(decode));
    }

    @Test
    public void testRC2EndoceDecode() {
        String secret = "i'm going to be encrypted";
        String rc2Material = "123456";
        String rc2Algorithm = "RC2";
        String encode = EncryptionUtils.encode(secret, rc2Material, rc2Algorithm);
        assertNotNull(encode);
        String decode = EncryptionUtils.decode(encode, rc2Material, rc2Algorithm);
        assertNotNull(encode);
        assertTrue(secret.equals(decode));
    }
}
