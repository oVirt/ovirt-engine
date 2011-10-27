package org.ovirt.engine.core.compat;

import org.ovirt.engine.core.compat.backendcompat.Convert;

import junit.framework.TestCase;

public class ConvertTest extends TestCase {
    public void testBoth() {
        String source = "dlkjslfjds reiwur 3\t \n dskjlfsjd lkjs lkfj";
        String b64 = Convert.ToBase64String(source);
        assertEquals(source, new String(Convert.FromBase64String(b64)));
    }

    public void testBothWithBytes() {
        String source = "dlkjslfjds reiwur 3\t \n dskjlfsjd lkjs lkfj";
        String b64 = Convert.ToBase64String(source.getBytes());
        assertEquals(source, new String(Convert.FromBase64String(b64)));
    }

    public void testFrom64() {
        assertEquals("My name is Jar Jar Binks.",
                new String(Convert.FromBase64String("TXkgbmFtZSBpcyBKYXIgSmFyIEJpbmtzLg==")));
    }

    public void testTo64() {
        assertEquals("TXkgbmFtZSBpcyBKYXIgSmFyIEJpbmtzLg==", Convert.ToBase64String("My name is Jar Jar Binks."));
    }
}
