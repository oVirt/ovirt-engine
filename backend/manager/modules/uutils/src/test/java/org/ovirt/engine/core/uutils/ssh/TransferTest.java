package org.ovirt.engine.core.uutils.ssh;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Long transfer test.
 */
public class TransferTest {
    String remote;
    static File local1;
    File local2;
    SSHClient client;

    byte[] digestFile(File file) throws FileNotFoundException, IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (InputStream is =  new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = is.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }

    @BeforeClass
    public static void init() throws IOException {

        TestCommon.initialize();

        local1 = File.createTempFile("ssh-test-", ".tmp");

        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (OutputStream os = new FileOutputStream(local1)) {
            byte[] buffer = new byte[1000];
            for (long i=0;i<TestCommon.largeTestFileSize / buffer.length;i++) {
                random.nextBytes(buffer);
                os.write(buffer);
            }
        }
    }

    @AfterClass
    public static void cleanUp() {
        if (local1 != null) {
            if (!local1.delete()) {
                // void
            }
            local1 = null;
        }
        TestCommon.terminate();
    }

    @Before
    public void setUp() throws IOException, Exception {
        remote = String.format("/tmp/ssh-test-%1$s.tmp", System.currentTimeMillis());
        local2 = File.createTempFile("ssh-test-", ".tmp");

        client = new SSHClient();
        client.setSoftTimeout(5 * 60 * 1000);
        client.setHardTimeout(TestCommon.largeTestHardTimeout);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
        client.setPassword(TestCommon.password);
        client.connect();
        client.authenticate();
    }

    @After
    public void tearDown() {
        if (client != null) {
            try {
                client.executeCommand(String.format("rm -f '%1$s'", remote), null, null, null);
            }
            catch(Exception e) {}
            try {
                client.close();
            }
            catch(Exception e) {}
            client = null;
        }
        if (local2 != null) {
            if (!local2.delete()) {
                // void
            }
            local2 = null;
        }
    }

    @Test
    public void testSendReceive() throws Exception {
        client.sendFile(local1.getAbsolutePath(), remote);
        client.receiveFile(remote, local2.getAbsolutePath());
        assertArrayEquals(
            digestFile(local1),
            digestFile(local2)
        );
    }

    @Test(expected=FileNotFoundException.class)
    public void testSendInvalidSource() throws Exception {
        client.sendFile(local1.getAbsolutePath()+"A", remote);
    }

    @Test(expected=IOException.class)
    public void testRecieveInvalidSource() throws Exception {
        client.receiveFile(remote+"A", local2.getAbsolutePath());
    }

    @Test(expected=IOException.class)
    public void testSendInvalidDestination() throws Exception {
        client.sendFile(local1.getAbsolutePath(), "/none/exist/path/file");
    }

    @Test(expected=IOException.class)
    public void testRecieveInvalidDestination() throws Exception {
        client.receiveFile("/none/exist/path/file", local2.getAbsolutePath());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSendInvalidFile() throws Exception {
        client.sendFile(local1.getAbsolutePath(), remote+"'");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRecieveInvalidFile() throws Exception {
        client.receiveFile(remote+"'", local2.getAbsolutePath());
    }
}
