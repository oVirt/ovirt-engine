package org.ovirt.engine.core.compat;

import java.io.StringReader;

import junit.framework.TestCase;

import org.ovirt.engine.core.compat.backendcompat.TextReaderCompat;

public class TextReaderCompatTest extends TestCase {
    public void testReadLine() {
        String foo = "This is a test.\nA real test.";
        StringReader reader = new StringReader(foo);
        TextReaderCompat compat = new TextReaderCompat(reader);
        assertEquals("first line", "This is a test.", compat.ReadLine());
        assertEquals("second line", "A real test.", compat.ReadLine());
    }
}
