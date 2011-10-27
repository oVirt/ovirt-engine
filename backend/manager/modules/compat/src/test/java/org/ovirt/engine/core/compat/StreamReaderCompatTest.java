package org.ovirt.engine.core.compat;

import java.io.StringReader;

import junit.framework.TestCase;

import org.ovirt.engine.core.compat.backendcompat.StreamReaderCompat;

public class StreamReaderCompatTest extends TestCase {
    public void testReadLine() {
        String foo = "This is a test.\nA real test.";
        StringReader reader = new StringReader(foo);
        StreamReaderCompat compat = new StreamReaderCompat(reader);
        assertEquals("first line", "This is a test.", compat.ReadLine());
        assertEquals("second line", "A real test.", compat.ReadLine());
    }

    public void testReadToEnd1() {
        String foo = "This is a test.\nA real test.";
        StringReader reader = new StringReader(foo);
        StreamReaderCompat compat = new StreamReaderCompat(reader);
        assertEquals("first line", "This is a test.", compat.ReadLine());
        assertEquals("second line", "A real test.", compat.ReadToEnd());
    }

    public void testReadToEnd2() {
        String foo = "This is a test.\nA real test.";
        StringReader reader = new StringReader(foo);
        StreamReaderCompat compat = new StreamReaderCompat(reader);
        assertEquals("the whole thing", foo, compat.ReadToEnd());
    }

    public void testDisposeAndExceptions() {

        boolean exception = false;
        try {
            String foo = "This is a test.\nA real test.";
            StringReader reader = new StringReader(foo);
            StreamReaderCompat compat = new StreamReaderCompat(reader);
            compat.ReadLine();
            compat.Dispose();
            compat.ReadLine();
        } catch (CompatException e) {
            exception = true;
        }
        assertTrue("Dispose should make subsequent reads fail", exception);
    }
}
