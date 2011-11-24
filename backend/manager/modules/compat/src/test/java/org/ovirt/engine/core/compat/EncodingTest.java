package org.ovirt.engine.core.compat;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class EncodingTest extends TestCase {
    public void testAscii() {
        Encoding enc = Encoding.ASCII;
        final String source = "8278473247 237429 3i21h31uyt4 3 \t \n";
        byte[] bytes = enc.getBytes(source);
        assertEquals(source, enc.getString(bytes));
    }

    public void testUTF8() {
        Encoding enc = Encoding.UTF8;
        final String source = "8278473247 237429 3i21h31uyt4 3 \t \n";
        byte[] bytes = enc.getBytes(source);
        assertEquals(source, enc.getString(bytes));
    }

    public void testBothWithBytes() {
        Encoding enc = Encoding.Base64;
        final String source = "dlkjslfjds reiwur 3\t \n dskjlfsjd lkjs lkfj";
        String base64String = enc.getString(source.getBytes());
        assertEquals(source, new String(enc.getBytes(base64String)));
    }

    public void testFrom64() {
        Encoding enc = Encoding.Base64;
        assertEquals("My name is Jar Jar Binks.",
                new String(enc.getBytes("TXkgbmFtZSBpcyBKYXIgSmFyIEJpbmtzLg==")));
    }

    public void testTo64() throws UnsupportedEncodingException {
        Encoding enc = Encoding.Base64;
        final byte[] byteArray = "My name is Jar Jar Binks.".getBytes();
        assertEquals("TXkgbmFtZSBpcyBKYXIgSmFyIEJpbmtzLg==", enc.getString(byteArray));
    }
}
