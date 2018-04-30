package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import javax.naming.TimeLimitExceededException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Command tests.
 */
public class CommandTest extends TestCommon {
    static final int softTimeout = 10 * 1000;
    static final int hardTimeout = 40 * 1000;
    SSHClient client;

    @BeforeEach
    public void setUp() throws Exception {
        client = new SSHClient();
        client.setSoftTimeout(softTimeout);
        client.setHardTimeout(hardTimeout);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
        client.setPassword(TestCommon.password);
        client.connect();
        client.authenticate();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test
    public void testCommandZero() throws Exception {
        client.executeCommand("true", null, null, null);
    }

    @Test
    public void testCommandNonZero() {
        assertThrows(IOException.class, () -> client.executeCommand("false", null, null, null));
    }

    @Test
    public void testCommandSignal() {
        assertThrows(IOException.class, () -> client.executeCommand("kill $$ ; sleep 10", null, null, null));
    }

    @Test
    public void testEchoStdout() throws Exception {
        String content = "hello\nworld!\nother\ndata";
        try (
                final InputStream stdin = new ByteArrayInputStream(content.getBytes("UTF-8"));
                final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                final ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {
            client.executeCommand("cat", stdin, stdout, stderr);
            assertEquals(content, new String(stdout.toByteArray(), "UTF-8"));
            assertEquals(0, stderr.size());
        }
    }

    @Test
    public void testEchoStderr() throws Exception {
        String content = "hello\nworld!\nother\ndata";
        try (
                final InputStream stdin = new ByteArrayInputStream(content.getBytes("UTF-8"));
                final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                final ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {
            client.executeCommand("cat >&2", stdin, stdout, stderr);
            assertEquals(content, new String(stderr.toByteArray(), "UTF-8"));
            assertEquals(0, stdout.size());
        }
    }

    @Test
    public void testEchoBoth() throws Exception {
        String content = "hello\nworld!\nother\ndata";
        try (
                final InputStream stdin = new ByteArrayInputStream(content.getBytes("UTF-8"));
                final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                final ByteArrayOutputStream stderr = new ByteArrayOutputStream()) {
            client.executeCommand("cat | tee /proc/self/fd/2", stdin, stdout, stderr);
            assertEquals(content, new String(stdout.toByteArray(), "UTF-8"));
            assertEquals(content, new String(stderr.toByteArray(), "UTF-8"));
        }
    }

    /* Expected harmless exception of sshd (if used) */
    @Test
    public void testSoftTimeout() throws Exception {
        long start = System.currentTimeMillis();
        try {
            client.executeCommand(String.format("sleep %d", softTimeout / 1000 * 4), null, null, null);
        } catch (TimeLimitExceededException e) {
            assertTrue(System.currentTimeMillis() - start >= softTimeout);
            assertTrue(System.currentTimeMillis() - start < softTimeout * 3 / 2);
        }
    }

    /* Expected harmless exception of sshd (if used) */
    @Test
    public void testHardTimeout() {
        assertTimeout(Duration.ofMinutes(2L), () -> {
            long start = System.currentTimeMillis();
            try {
                client.executeCommand(
                        String.format("while true; do echo sleeping; sleep %d; done", softTimeout / 1000 / 2),
                        null,
                        null,
                        null);
            } catch (TimeLimitExceededException e) {
                assertTrue(System.currentTimeMillis() - start >= hardTimeout);
                assertTrue(System.currentTimeMillis() - start < hardTimeout * 3 / 2);
            }
        });
    }
}
