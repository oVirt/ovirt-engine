package org.ovirt.engine.core.utils.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.BeforeClass;
import org.junit.Test;

public class EngineEncryptionUtilsTest {

    @BeforeClass
    public static void before() throws UnsupportedEncodingException {
        EngineEncryptionUtils.keystoreFile = new File(URLDecoder.decode(ClassLoader.getSystemResource("key.p12").getPath(), "UTF-8"));
        EngineEncryptionUtils.keystorePassword = new KeyStore.PasswordProtection("NoSoup4U".toCharArray());
        EngineEncryptionUtils.keystoreAlias = "1";
    }

    @Test
    public void testEncrypt() throws Exception {
        String plain = "Test123!32@";
        String encrypted = EngineEncryptionUtils.encrypt(plain);
        String plain2 = EngineEncryptionUtils.decrypt(encrypted);
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
                                String encrypted = EngineEncryptionUtils.encrypt(plain);
                                String actualDecrypted = EngineEncryptionUtils.decrypt(encrypted);
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
