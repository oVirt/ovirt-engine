package org.ovirt.engine.core.compat;

import org.ovirt.engine.core.compat.backendcompat.Environment;

import junit.framework.TestCase;

public class EnvironmentTest extends TestCase {
    public void testMachineName() {
        String machineName = Environment.MachineName;
        assertNotNull("The name should not be null", machineName);

        String realName = null;
        if (System.getProperty("os.name").startsWith("Win")) {
            realName = System.getenv("COMPUTERNAME");
        } else {
            realName = System.getenv("HOSTNAME");
        }
        if (realName == null)
        {
            try
            {
                realName = java.net.InetAddress.getLocalHost().getHostName();
            }
            catch (Exception e) { }
        }

        assertEquals("The name should be the hostname ENV variable", realName, machineName);
    }
}
