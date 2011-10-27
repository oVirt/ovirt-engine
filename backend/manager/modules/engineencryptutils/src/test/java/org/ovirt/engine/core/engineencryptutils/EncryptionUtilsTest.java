package org.ovirt.engine.core.engineencryptutils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.ovirt.engine.core.login.EngineSecureIdentityLoginModule;

public class EncryptionUtilsTest {

    public void testEncryptDecrypt() {
        /*
         * try { String plain = "123456"; String keyFile = "/tmp/ca/.keystore"; String alias = "engine"; String encrypted
         * = EncryptionUtils.encrypt(plain, keyFile, "NoSoup4U", alias); String decrypted =
         * EncryptionUtils.decrypt(encrypted, keyFile, "NoSoup4U", alias); System.out.println("decrypted: " + decrypted);
         * assertEquals(plain, decrypted); } catch (Exception e) { fail(); }
         */
    }

    public void testMSCompat() {
        /*
         * String plain = "Hello World"; String cipher =
         * "cT7TWp5AGMJwz7420qCpZOnefuP7QxFrrmrNkn5uxaIY3ZWYHFZimCWx2dmiFdFSryFcOj1G/ZULTDiP8XfwJk2CWIk38B5I9JtmWUCUVNt7+y09BF4/b8Bg+XVXM1GzUFdxMggvicp16uRxiqGJhG8vqNHTTEt5Pv9q0/LVf4U="
         * ; String keyPath = "ca.pfx"; RefObject<String> error = new RefObject<String>(); String decrypted =
         * EncryptionUtils.Decrypt(cipher, keyPath, "NoSoup4U", error); System.out.println("decrypted: " + decrypted);
         * assertEquals(plain, decrypted);
         */
    }

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

    @Test
    public void testMainEndoceDecode() throws Exception {
        String[] args = { "ENGINEadmin2009!"};
        EngineSecureIdentityLoginModule.main(args);

        args = new String[] { "ENGINEadmin2009!", "jaas is the way", "Blowfish" };
        EngineSecureIdentityLoginModule.main(args);


        args = new String[] { "ENGINEadmin2009!", "some passphrase", "Blowfish" };
        EngineSecureIdentityLoginModule.main(args);

        args = new String[] { "ENGINEadmin2009!", "12345678$#", "RC2" };
        EngineSecureIdentityLoginModule.main(args);
    }
}
