package org.ovirt.engine.core.compat;

import junit.framework.TestCase;

public class EncodingTest extends TestCase {
    public void testAscii() {
        Encoding enc = Encoding.ASCII;
        String source = "8278473247 237429 3i21h31uyt4 3 \t \n";
        byte[] bytes = enc.GetBytes(source);
        assertEquals(source, enc.GetString(bytes));
        ;
    }

    public void testUTF8() {
        Encoding enc = Encoding.UTF8;
        String source = "8278473247 237429 3i21h31uyt4 3 \t \n";
        byte[] bytes = enc.GetBytes(source);
        assertEquals(source, enc.GetString(bytes));
        ;
    }
}
