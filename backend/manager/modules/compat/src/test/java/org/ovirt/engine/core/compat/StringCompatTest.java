package org.ovirt.engine.core.compat;

import junit.framework.TestCase;

public class StringCompatTest extends TestCase {
    public void testIt() {
        String value = "Jar Jar Binks is Cool";
        String[] parts = value.split(" ");
        assertEquals(value, StringHelper.join(" ", parts));
    }
}
