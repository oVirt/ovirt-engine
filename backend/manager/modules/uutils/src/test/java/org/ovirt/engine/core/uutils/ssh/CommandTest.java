package org.ovirt.engine.core.uutils.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.naming.TimeLimitExceededException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Command tests.
 */
public class CommandTest {
    static final int softTimeout = 10*1000;
    static final int hardTimeout = 40*1000;
    SSHClient client;

    @BeforeClass
    public static void init() {
        TestCommon.initialize();
    }

    @AfterClass
    public static void cleanUp() {
        TestCommon.terminate();
    }

    @Before
    public void setUp() throws IOException, Exception {
        client = new SSHClient();
        client.setSoftTimeout(softTimeout);
        client.setHardTimeout(hardTimeout);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
        client.setPassword(TestCommon.password);
        client.connect();
        client.authenticate();
    }

    @After
    public void tearDown() {
        try {
            if (client != null) {
                client.close();
                client = null;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCommandZero() throws Exception {
        client.executeCommand("true", null, null, null);
    }

    @Test(expected=IOException.class)
    public void testCommandNonZero() throws Exception {
        client.executeCommand("false", null, null, null);
    }

    @Test(expected=IOException.class)
    public void testCommandSignal() throws Exception {
        client.executeCommand("kill $$ ; sleep 10", null, null, null);
    }

    @Test
    public void testEchoStdout() throws Exception {
        String content = "hello\nworld!\nother\ndata";
        try (
            final InputStream stdin = new ByteArrayInputStream(content.getBytes("UTF-8"));
            final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ) {
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
            final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ) {
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
            final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ) {
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
            client.executeCommand(String.format("sleep %d", softTimeout/1000*4), null, null, null);
        }
        catch(TimeLimitExceededException e) {
            assertTrue(System.currentTimeMillis() - start >= softTimeout);
            assertTrue(System.currentTimeMillis() - start < softTimeout*3/2);
        }
    }

    /* Expected harmless exception of sshd (if used) */
    @Test(timeout=120*1000)
    public void testHardTimeout() throws Exception {
        long start = System.currentTimeMillis();
        try {
            client.executeCommand(String.format("while true; do echo sleeping; sleep %d; done", softTimeout/1000/2), null, null, null);
        }
        catch(TimeLimitExceededException e) {
            assertTrue(System.currentTimeMillis() - start >= hardTimeout);
            assertTrue(System.currentTimeMillis() - start < hardTimeout*3/2);
        }
    }
}
