package org.ovirt.engine.core.utils;

import junit.framework.TestCase;

public class ObjectDescriptorTest extends TestCase {
    public void testIt() {
        Jedi jedi = new Jedi();
        String desc = ObjectDescriptor.toString(jedi);
        System.out.println(desc);
        assertTrue("Anakin", desc.contains("Anakin"));
        assertTrue("name", desc.contains("name"));
        assertTrue("Blue", desc.contains("Blue"));
        assertTrue("saberColor", desc.contains("saberColor"));
        assertTrue("Class", desc.contains("Class"));
        assertTrue("org.ovirt.engine.core.utils.Jedi", desc.contains("org.ovirt.engine.core.utils.Jedi"));
    }
}
