package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Basic tests.
 *
 * Authentication and sanity.
 */
public class PropertiesTest {
    @Test
    public void testProperties() throws IOException {
        try (final SSHClient ssh = new SSHClient()) {
            assertNull(ssh.getHost());
            assertEquals(22, ssh.getPort());
            assertNull(ssh.getUser());
            assertEquals("N/A", ssh.getDisplayHost());

            ssh.setHost("host1");
            assertEquals("host1", ssh.getHost());
            assertEquals("host1", ssh.getDisplayHost());
            ssh.setHost("host1", 1234);
            assertEquals(1234, ssh.getPort());
            assertEquals("host1:1234", ssh.getDisplayHost());
            ssh.setUser("user1");
            assertEquals("user1", ssh.getUser());
            assertEquals("user1@host1:1234", ssh.getDisplayHost());
            ssh.setHost("host2");
            assertEquals("user1@host2", ssh.getDisplayHost());
        }
    }
}
