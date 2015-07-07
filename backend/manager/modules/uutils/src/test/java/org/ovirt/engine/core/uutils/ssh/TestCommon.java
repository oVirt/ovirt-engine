package org.ovirt.engine.core.uutils.ssh;

/*
 * Example of execution:
 *  $ mvn -Dssh-host=host1 -Dssh-test-port=22 -Dssh-test-user=root -Dssh-test-password=password -Dssh-test-p12=a.p12 -Dssh-test-p12-password=password -Dssh-test-large-file-size=50000000
 *
 * Standalone testing:
 *  $ mkdir t
 *  $ cp ~/.m2/repository/junit/junit/4.8.1/junit-4.8.1.jar t/
 *  $ cp ~/.m2/repository/org/apache/mina/mina-core/2.0.4/mina-core-2.0.4.jar t/
 *  $ cp ~/.m2/repository/org/apache/sshd/sshd-core/0.7.0/sshd-core-0.7.0.jar t/
 *  $ cp ~/.m2/repository/org/mockito/mockito-core/1.8.5/mockito-core-1.8.5.jar t/
 *  $ cp ~/.m2/repository/org/objenesis/objenesis/1.2/objenesis-1.2.jar t
 *  $ cp ~/.m2/repository/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar t
 *  $ cp ~/.m2/repository/org/slf4j/slf4j-simple/1.6.4/slf4j-simple-1.6.4.jar t
 *  $ javac -d t -cp t/*:. ./backend/manager/modules/utils/src/{main,test}/java/org/ovirt/engine/core/utils/ssh/*.java
 *  $ ( cd t && java -cp *:. org.ovirt.engine.core.utils.ssh.TestCommon )
 *
 * Create PKCS#12:
 * $ openssl req -newkey rsa:2048 -x509 -days 365 -out a.crt -keyout a.key
 * $ openssl pkcs12 -export -inkey a.key -in a.crt -out a.p12
 *
 * Extract openssh public from PKCS#12:
 * $ openssl pkcs12 -in /tmp/a.p12 -nokeys | openssl x509 -pubkey -noout | ssh-keygen -i -m PKCS8 -f /proc/self/fd/0
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Test class for SSHClient.
 */
public class TestCommon {

    static String host;
    static int port;
    static String user;
    static String password;
    static KeyPair keyPair;
    static long largeTestFileSize;
    static long largeTestHardTimeout;
    static SSHD sshd;

    public static void initialize() {
        host = System.getProperty("ssh-host");

        if (host == null) {
                System.out.println("WARNING: using internal daemon");
                try {
                    keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
                }
                catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                host = "localhost";
                user = "root";
                password = "password";
                sshd = new SSHD();
                sshd.setUser(user, password, keyPair.getPublic());
                try {
                        sshd.start();
                }
                catch(IOException e) {
                        throw new RuntimeException(e);
                }
                port = sshd.getPort();
        }
        else {
                port = Integer.parseInt(System.getProperty("ssh-test-port", "22"));
                user = System.getProperty("ssh-test-user", "root");
                password = System.getProperty("ssh-test-password", "password");
                String p12 = System.getProperty("ssh-test-p12");
                String p12_password = System.getProperty("ssh-test-p12-password", "password");

                FileInputStream fis = null;
                try {
                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    fis = new FileInputStream(p12);
                    keyStore.load(fis, p12_password.toCharArray());
                    KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry("1", new KeyStore.PasswordProtection(p12_password.toCharArray()));
                    keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
                }
                catch(Throwable t) {
                    throw new RuntimeException(t);
                }
                finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
        }

        largeTestFileSize = Long.parseLong(System.getProperty("ssh-test-large-file-size", "50000000"));
        largeTestHardTimeout = Long.parseLong(System.getProperty("ssh-test-long-hard-timeout", "0"));
    }

    /**
     * Terminate.
     */
    public static void terminate() {
        if (sshd != null) {
            sshd.stop();
            sshd = null;
        }
    }

    /**
     * main.
     *
     * @param args arguments.
     *
     * Use system properities to alternate behvaior.
     */
    public static void main (String [] args) throws Throwable {

        initialize();

        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        Result result = core.run(
            ConstraintByteArrayOutputStreamTest.class,
            BasicTest.class,
            CommandTest.class,
            TimeoutTest.class,
            TransferTest.class
        );
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
