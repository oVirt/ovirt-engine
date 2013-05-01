package org.ovirt.engine.core.utils.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

public class EncryptionUtilsTest {

    private static String keyStoreURL;
    private static String keyStorePass = "NoSoup4U";
    private static String certAlias = "1";

    @Before
    public void before() throws UnsupportedEncodingException {
        keyStoreURL = URLDecoder.decode(ClassLoader.getSystemResource("key.p12").getPath(), "UTF-8");
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
    public void testEncrypt() throws Exception {
        String plain = "Test123!32@";
        String encrypted = EncryptionUtils.encrypt(plain, keyStoreURL, keyStorePass, certAlias);
        String plain2 = EncryptionUtils.decrypt(encrypted, keyStoreURL, keyStorePass, certAlias);
        assertEquals(plain, plain2);
    }

    @Test
    public void testEncryptThreads() throws Exception {
        List<Thread> l = new LinkedList<Thread>();
        final String plain = "Test123!32@";
        final AtomicBoolean failed = new AtomicBoolean();

        // StringBuffer is used instead of StringBuilder since it's thread-safe
        final StringBuffer failures = new StringBuffer();
        for (int i = 0; i < 100; i++) {
            final int threadCount = i;
            l.add (
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String encrypted = EncryptionUtils.encrypt(plain, keyStoreURL, keyStorePass, certAlias);
                                String actualDecrypted = EncryptionUtils.decrypt(encrypted, keyStoreURL, keyStorePass, certAlias);
                                if (!plain.equals(actualDecrypted)) {
                                    String failure = String.format("Failure in test %d, plain is %s%n",
                                                            threadCount,
                                                            actualDecrypted);
                                    failures.append(failure);
                                    failed.set(true);
                                }
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                )
            );
        }

        for (Thread t : l) {
            t.start();
        }
        for (Thread t : l) {
            t.join();
        }

        assertFalse(failures.toString(), failed.get());
    }
}
