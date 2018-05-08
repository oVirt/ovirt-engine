package org.ovirt.engine.core.uutils.ssh;

/*
 * Example of execution:
 *  $ mvn -Dssh-host=host1 -Dssh-test-port=22 -Dssh-test-user=root -Dssh-test-password=password -Dssh-test-p12=a.p12 -Dssh-test-p12-password=password -Dssh-test-large-file-size=50000000
 *
 * Create PKCS#12:
 * $ openssl req -newkey rsa:2048 -x509 -days 365 -out a.crt -keyout a.key
 * $ openssl pkcs12 -export -inkey a.key -in a.crt -out a.p12
 *
 * Extract openssh public from PKCS#12:
 * $ openssl pkcs12 -in /tmp/a.p12 -nokeys | openssl x509 -pubkey -noout | ssh-keygen -i -m PKCS8 -f /proc/self/fd/0
 */

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for SSHClient.
 */
@Tag("ssh")
public class TestCommon {

    private static Logger log = LoggerFactory.getLogger(TestCommon.class);

    static String host;
    static int port;
    static String user;
    static String password;
    static KeyPair keyPair;
    static long largeTestFileSize;
    static long largeTestHardTimeout;
    static SSHD sshd;

    @BeforeAll
    public static void initialize() throws Exception {
        host = System.getProperty("ssh-host");

        if (host == null) {
            log.warn("WARNING: using internal daemon");
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            host = "localhost";
            user = "root";
            password = "password";
            sshd = new SSHD();
            sshd.setUser(user, password, keyPair.getPublic());
            sshd.start();
            port = sshd.getPort();
        } else {
            port = Integer.parseInt(System.getProperty("ssh-test-port", "22"));
            user = System.getProperty("ssh-test-user", "root");
            password = System.getProperty("ssh-test-password", "password");
            String p12 = System.getProperty("ssh-test-p12");
            String p12_password = System.getProperty("ssh-test-p12-password", "password");

            try (FileInputStream fis = new FileInputStream(p12)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(fis, p12_password.toCharArray());
                KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("1",
                        new KeyStore.PasswordProtection(p12_password.toCharArray()));
                keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
            }
        }

        largeTestFileSize = Long.parseLong(System.getProperty("ssh-test-large-file-size", "50000000"));
        largeTestHardTimeout = Long.parseLong(System.getProperty("ssh-test-long-hard-timeout", "0"));
    }

    /**
     * Terminate.
     */
    @AfterAll
    public static void terminate() {
        if (sshd != null) {
            sshd.stop();
            sshd = null;
        }
    }
}
