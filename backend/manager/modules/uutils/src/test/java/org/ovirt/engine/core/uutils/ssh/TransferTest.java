package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Long transfer test.
 */
public class TransferTest extends TestCommon {
    String remote;
    static File local1;
    File local2;
    SSHClient client;

    byte[] digestFile(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = is.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }

    @BeforeAll
    public static void init() throws IOException {
        local1 = File.createTempFile("ssh-test-", ".tmp");

        SecureRandom random = new SecureRandom();
        try (OutputStream os = new FileOutputStream(local1)) {
            byte[] buffer = new byte[1000];
            for (long i = 0; i < TestCommon.largeTestFileSize / buffer.length; i++) {
                random.nextBytes(buffer);
                os.write(buffer);
            }
        }
    }

    @AfterAll
    public static void cleanUp() {
        if (local1 != null) {
            if (!local1.delete()) {
                // void
            }
            local1 = null;
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
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

    @AfterEach
    public void tearDown() {
        if (client != null) {
            try {
                client.executeCommand(String.format("rm -f '%1$s'", remote), null, null, null);
            } catch (Exception ignore) {
            }
            try {
                client.close();
            } catch (Exception ignore) {
            }
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
                digestFile(local2));
    }

    @Test
    public void testSendInvalidSource() {
        assertThrows(FileNotFoundException.class, () -> client.sendFile(local1.getAbsolutePath() + "A", remote));
    }

    @Test
    public void testRecieveInvalidSource() {
        assertThrows(IOException.class, () -> client.receiveFile(remote + "A", local2.getAbsolutePath()));
    }

    @Test
    public void testSendInvalidDestination() {
        assertThrows(IOException.class, () -> client.sendFile(local1.getAbsolutePath(), "/none/exist/path/file"));
    }

    @Test
    public void testRecieveInvalidDestination() {
        assertThrows(IOException.class, () -> client.receiveFile("/none/exist/path/file", local2.getAbsolutePath()));
    }

    @Test
    public void testSendInvalidFile() {
        assertThrows(IllegalArgumentException.class, () -> client.sendFile(local1.getAbsolutePath(), remote + "'"));
    }

    @Test
    public void testRecieveInvalidFile() {
        assertThrows(IllegalArgumentException.class, () -> client.receiveFile(remote + "'", local2.getAbsolutePath()));
    }
}
