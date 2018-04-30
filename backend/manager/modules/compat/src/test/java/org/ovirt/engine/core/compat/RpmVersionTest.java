package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class RpmVersionTest {

    @Test
    public void rpmVersionTest() {
        assertEquals("2.3.235.0", new RpmVersion("rhev-agent-2.3.235-1.el6").getValue());
        assertEquals("2.3.17.0", new RpmVersion("rhev-agent-2.3.17-1.el6").getValue());
        assertEquals("2.133.4.5", new RpmVersion("glibc-devel-2.133.4.5-1.x86_64").getValue());
        assertEquals("10.6.2.0", new RpmVersion("test-javadb-common-10.6.2-1.1.i386").getValue());
        assertEquals("10.6.2.3", new RpmVersion("test-javadb-common-10.6.2.3.3-1.1.i386").getValue());
        assertEquals(0, new RpmVersion("").getValue().length());
        assertEquals(0, new RpmVersion(null).getValue().length());
    }

    @Test
    public void agentVersionTest() {
        assertEquals("2.3.395.0", new RpmVersion("RHEV-Agent 2.3.395", "RHEV-Agent", true).getValue());
        assertEquals("2.2.0.0", new RpmVersion("rhev-agent-2.2-1.el6", "RHEV-Agent", true).getValue());
        assertEquals("2.3.7.0", new RpmVersion("rhev-agent-2.3.7-1.el6", "RHEV-Agent", true).getValue());
        assertEquals("2.3.7.10", new RpmVersion("rhev-agent-2.3.7.10-1.el6", "RHEV-Agent", true).getValue());
        assertEquals("2.3.7.10", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true).getValue());
        assertEquals("2.3.7.10", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "rhev-agent", false).getValue());
        assertEquals("2.3.7.10", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false).getValue());
        assertNotEquals("0.0.0.0", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false).getValue());
        assertEquals(0, new RpmVersion("", "", false).getValue().length());
        assertEquals(0, new RpmVersion(null, null, false).getValue().length());
    }

    @Test
    public void caseSensitiveTest() {
        RpmVersion version1 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false);
        RpmVersion version2 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true);
        assertEquals(version1, version2);
    }

    @Test
    public void testRpmRelease() {
        assertEquals("20130212.fc18.noarch",
                new RpmVersion("ovirt-node-iso-2.6.0-20130212.fc18.noarch").getRpmRelease());
        assertEquals("1.1.i386", new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386").getRpmRelease());
        assertEquals("2.3.10.4.fc18.x86_64",
                new RpmVersion("java-1.7.0-openjdk-devel-1.7.0.25-2.3.10.4.fc18.x86_64").getRpmRelease());
        assertEquals("1.el6", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", true).getRpmRelease());
        assertEquals("1.el6", new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "rhev-agent", false).getRpmRelease());
    }

    @Test
    public void equalTest() {
        RpmVersion rpm1 = new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386");
        RpmVersion rpm2 = new RpmVersion("test-javadb-common-10.2.2.2.2-1.1.i386");
        RpmVersion rpm3 = new RpmVersion("test-javadb-common-10.2.2.2.3-1.1.i386");
        assertEquals(rpm1, rpm2);
        assertEquals(rpm2, rpm1);
        assertNotEquals(rpm2, rpm3);
        assertNotEquals(rpm3, rpm2);

        RpmVersion version1 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "RHEV-Agent", false);
        RpmVersion version2 = new RpmVersion("rhev-agent-2.3.7.10.3-2.el6", "RHEV-Agent", false);
        assertNotEquals(version1, version2);

        RpmVersion version3 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", "", false);
        RpmVersion version4 = new RpmVersion("rhev-agent-2.3.7.10.3-1.el6", null, false);
        assertEquals(version3, version4);

        RpmVersion version5 = new RpmVersion(null, null, false);
        RpmVersion version6 = new RpmVersion(null, null, false);
        assertEquals(version5, version6);
    }
}
