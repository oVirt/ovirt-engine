package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RpmVersionTest {

    @Test
    public void rpmVersionTest() {
        assertEquals(new RpmVersion("rhev-agent-2.3.235-1.el6").getValue(), "2.3.235.0");
        assertEquals(new RpmVersion("rhev-agent-2.3.17-1.el6").getValue(), "2.3.17.0");
        assertEquals(new RpmVersion("glibc-devel-2.133.4.5-1.x86_64").getValue(), "2.133.4.5");
        assertEquals(new RpmVersion("test-javadb-common-10.6.2-1.1.i386").getValue(), "10.6.2.0");
        assertEquals(new RpmVersion("test-javadb-common-10.6.2.3.3-1.1.i386").getValue(), "10.6.2.3");
        assertTrue(new RpmVersion("").getValue().length() == 0);
        assertTrue(new RpmVersion(null).getValue().length() == 0);
    }

    @Test
    public void agentVersionTest() {
        assertEquals(new RpmVersion("RHEV-Agent 2.3.395", "RHEV-Agent", true).getValue(), "2.3.395.0");
        assertEquals(new RpmVersion("rhev-agent-2.2-1.el6", "RHEV-Agent", true).getValue(), "2.2.0.0");
        assertEquals(new RpmVersion("rhev-agent-2.3.7-1.el6", "RHEV-Agent", true).getValue(), "2.3.7.0");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10-1.el6", "RHEV-Agent", true).getValue(), "2.3.7.10");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true).getValue(), "2.3.7.10");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "rhev-agent", false).getValue(), "2.3.7.10");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false).getValue(), "2.3.7.10");
        assertFalse("0.0.0.0".equals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false).getValue()));
        assertTrue(new RpmVersion("", "", false).getValue().length() == 0);
        assertTrue(new RpmVersion(null, null, false).getValue().length() == 0);
    }

    @Test
    public void caseSensitiveTest() {
        RpmVersion version1 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false);
        RpmVersion version2 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true);
        assertEquals(version1, version2);
    }

    @Test
    public void testRpmRevision() {
        assertEquals(new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386").getRpmRevision(), "1.1.i386");
        assertEquals(new RpmVersion("java-1.7.0-openjdk-devel-1.7.0.25-2.3.10.4.fc18.x86_64").getRpmRevision(),
                "2.3.10.4.fc18.x86_64");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true).getRpmRevision(), "1.el6");
        assertEquals(new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "rhev-agent", false).getRpmRevision(), "1.el6");
    }

    @Test
    public void equalTest() {
        RpmVersion rpm1 = new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386");
        RpmVersion rpm2 = new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386");
        RpmVersion rpm3 = new RpmVersion("test-javadb-common-10.2.2.2.3-1.1.i386");
        assertTrue(rpm1.equals(rpm2));
        assertTrue(rpm2.equals(rpm1));
        assertFalse(rpm2.equals(rpm3));
        assertFalse(rpm3.equals(rpm2));

        RpmVersion version1 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false);
        RpmVersion version2 = new RpmVersion("rhev-agent-2.3.7.10.3-2.el6", "RHEV-Agent", false);
        assertFalse(version1.equals(version2));

        RpmVersion version3 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "", false);
        RpmVersion version4 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", null, false);
        assertTrue(version3.equals(version4));

        RpmVersion version5 = new RpmVersion(null, null, false);
        RpmVersion version6 = new RpmVersion(null, null, false);
        assertTrue(version5.equals(version6));
    }
}
