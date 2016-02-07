package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class ValidationUtilsTest {
    @Test
    public void testcontainsIlegalCharacters() {
        assertPatternMatches("Valid strings: ", ValidationUtils.NO_SPECIAL_CHARACTERS, "www_redhat_com", "127001", "www_REDHAT_1");
        assertPatternDoesNotMatch("Invalid strings: ", ValidationUtils.NO_SPECIAL_CHARACTERS, "www.redhatcom", "me@localhost", "no/worries");
    }

    @Test
    public void testIsInvalidHostname() {
        String[] straValidHosts =
                new String[] { "blahblah", "www.redhat.com", "www.rhn.redhat.com", "127.0.0.1",
                        "1::2", "1:0002:34:4:5:6:7:8" };
        String[] straInvalidHosts =
                new String[] { "www.redhat#com", "123/456", "www@redhat.com", "www.řhň.řěďháť.čőm", "你好世界",
                        "שלוםעולם" };
        for (String s : straValidHosts) {
            assertTrue("Valid host name: " + s, ValidationUtils.validHostname(s));
        }

        for (String s : straInvalidHosts) {
            assertTrue("Invalid host name: " + s, !ValidationUtils.validHostname(s));
        }
    }

    @Test
    public void testTrimmingWhitespaces() {
        assertPatternMatches("Valid string (no trimming whitespaces): ", ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN, "", "aoeu", "a o e u ř", "%2123 o ^ ooe#");
        assertPatternDoesNotMatch("Invalid string (trimming whitespaces): ", ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN, " ", " aoeu", "a o e u ř ", " %2123 o ^ ooe##", " aoeu ");
    }

    @Test
    public void validUri() {
        assertTrue(ValidationUtils.validUri("a://asdasd:21"));
        assertTrue(ValidationUtils.validUri("a://asdasd"));
        assertTrue(ValidationUtils.validUri("asdasd:21"));
        assertTrue(ValidationUtils.validUri("asdasd"));
        assertTrue(ValidationUtils.validUri("1.2.3.4"));

        assertFalse(ValidationUtils.validUri("://asdasd:12"));
        assertFalse(ValidationUtils.validUri("asd asd"));
    }

    @Test
    public void testValidIsoPath() {
        assertPatternMatches("Valid isoPath: ", ValidationUtils.ISO_SUFFIX_PATTERN, "", "foo.iso", "RHEVM-123456-tools.iso");
        assertPatternDoesNotMatch("Invalid isoPath: ", ValidationUtils.ISO_SUFFIX_PATTERN, "x", "sysprep.vfd", "disk.ISO");
    }

    private void assertPatternMatches(String message, String pattern, String... validStrings) {
        for (String s : validStrings) {
            assertTrue(message + s, Pattern.matches(pattern, s));
        }
    }

    private void assertPatternDoesNotMatch(String message, String pattern, String... invalidStrings) {
        for (String s : invalidStrings) {
            assertTrue(message + s, !Pattern.matches(pattern, s));
        }
    }
}
