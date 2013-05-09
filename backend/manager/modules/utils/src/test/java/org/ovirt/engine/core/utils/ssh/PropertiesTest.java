package org.ovirt.engine.core.utils.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Basic tests.
 *
 * Authentication and sanity.
 */
public class PropertiesTest {
    @Test
    public void testProperties() {
        SSHClient ssh = null;
        try {
            ssh = new SSHClient();

            assertEquals(ssh.getHost(), null);
            assertEquals(ssh.getPort(), 22);
            assertEquals(ssh.getUser(), null);
            assertEquals(ssh.getDisplayHost(), "N/A");

            ssh.setHost("host1");
            assertEquals(ssh.getHost(), "host1");
            assertEquals(ssh.getDisplayHost(), "host1");
            ssh.setHost("host1", 1234);
            assertEquals(ssh.getPort(), 1234);
            assertEquals(ssh.getDisplayHost(), "host1:1234");
            ssh.setUser("user1");
            assertEquals(ssh.getUser(), "user1");
            assertEquals(ssh.getDisplayHost(), "user1@host1:1234");
            ssh.setHost("host2");
            assertEquals(ssh.getDisplayHost(), "user1@host2");
        }
        finally {
            if (ssh != null) {
                ssh.disconnect();
            }
        }
    }
}
