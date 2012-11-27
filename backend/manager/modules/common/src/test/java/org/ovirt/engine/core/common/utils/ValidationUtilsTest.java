package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class ValidationUtilsTest {
    @Test
    public void testcontainsIlegalCharacters() {
        String[] straValid = new String[] { "www_redhat_com", "127001", "www_REDHAT_1" };
        String[] straInvalid = new String[] { "www.redhatcom", "me@localhost", "no/worries" };
        for (String s : straValid) {
            assertTrue("Valid strings: " + s, !ValidationUtils.containsIlegalCharacters(s));
        }

        for (String s : straInvalid) {
            assertTrue("Invalid strings: " + s, ValidationUtils.containsIlegalCharacters(s));
        }
    }

    @Test
    public void testIsInvalidHostname() {
        String[] straValidHosts =
                new String[] { "www.redhat.com", "127.0.0.1", "www.rhn.redhat.com" };
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
        String[] validStrings = {"", "aoeu", "a o e u ř", "%2123 o ^ ooe#"};
        String[] inValidStrings = {" ", " aoeu", "a o e u ř ", " %2123 o ^ ooe##", " aoeu "};

        for (String s : validStrings) {
            assertTrue("Valid strings (no trimming whitespaces)",
                    Pattern.matches(ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN, s));
        }

        for (String s : inValidStrings) {
            assertTrue("Invalid strings (trimming whitespaces)",
                    !Pattern.matches(ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN, s));
        }
    }
}
