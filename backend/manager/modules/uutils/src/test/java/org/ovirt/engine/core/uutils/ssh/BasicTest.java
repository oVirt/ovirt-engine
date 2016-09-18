package org.ovirt.engine.core.uutils.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.naming.AuthenticationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic tests.
 *
 * Authentication and sanity.
 */
public class BasicTest extends TestCommon {
    static final String helloCommand = "echo test";
    static final String helloResult = "test\n";
    SSHClient client;

    @Before
    public void setUp() {
        client = new SSHClient();
        client.setSoftTimeout(30 * 1000);
        client.setHardTimeout(5 * 60 * 1000);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test(expected = AuthenticationException.class)
    public void testWrongPassword() throws Exception {
        client.setPassword(TestCommon.password + "A");
        client.connect();
        client.authenticate();
        client.executeCommand(helloCommand, null, null, null);
    }

    @Test(expected = AuthenticationException.class)
    public void testWrongKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        KeyPair badKeyPair = generator.generateKeyPair();
        client.setKeyPair(badKeyPair);
        client.connect();
        client.authenticate();
        client.executeCommand(helloCommand, null, null, null);
    }

    @Test
    public void testPassword() throws Exception {
        try (final ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(500)) {
            client.setPassword(TestCommon.password);
            client.connect();
            client.authenticate();
            client.executeCommand(helloCommand, null, out, null);
            assertEquals(helloResult, new String(out.toByteArray(), "UTF-8"));
        }
    }

    @Test
    public void testPK() throws Exception {
        try (final ByteArrayOutputStream out = new ConstraintByteArrayOutputStream(500)) {
            client.setKeyPair(TestCommon.keyPair);
            client.connect();
            client.authenticate();
            client.executeCommand(helloCommand, null, out, null);
            assertEquals(helloResult, new String(out.toByteArray(), "UTF-8"));
        }
    }

    @Test
    public void testHostKey() throws Exception {
        assumeNotNull(TestCommon.sshd);
        client.connect();
        assertEquals(TestCommon.sshd.getKey(), client.getHostKey());
    }
}
