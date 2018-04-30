package org.ovirt.engine.core.utils.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockEngineLocalConfigExtension;
import org.ovirt.engine.core.utils.TrustStoreTestUtils;

@ExtendWith(MockEngineLocalConfigExtension.class)
public class EngineEncryptionUtilsTest {

    public static Stream<Pair<String, String>> mockEngineLocalConfiguration() {
        return Stream.of(
                new Pair<>("ENGINE_PKI_TRUST_STORE_TYPE", "JKS"),
                new Pair<>("ENGINE_PKI_TRUST_STORE", TrustStoreTestUtils.getTrustStorePath()),
                new Pair<>("ENGINE_PKI_TRUST_STORE_PASSWORD", "NoSoup4U"),
                new Pair<>("ENGINE_PKI_ENGINE_STORE_TYPE", "PKCS12"),
                new Pair<>("ENGINE_PKI_ENGINE_STORE", TrustStoreTestUtils.getTrustStorePath()),
                new Pair<>("ENGINE_PKI_ENGINE_STORE_PASSWORD", "NoSoup4U"),
                new Pair<>("ENGINE_PKI_ENGINE_STORE_ALIAS", "1")
        );
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
        List<Thread> l = new LinkedList<>();
        final String plain = "Test123!32@";
        final AtomicBoolean failed = new AtomicBoolean();

        // StringBuffer is used instead of StringBuilder since it's thread-safe
        final StringBuffer failures = new StringBuffer();
        for (int i = 0; i < 100; i++) {
            final int threadCount = i;
            l.add(
                new Thread(() -> {
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
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
            );
        }

        for (Thread t : l) {
            t.start();
        }
        for (Thread t : l) {
            t.join();
        }

        assertFalse(failed.get(), failures.toString());
    }
}
