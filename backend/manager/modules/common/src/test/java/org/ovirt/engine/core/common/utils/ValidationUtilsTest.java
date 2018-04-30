package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

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
            assertTrue(ValidationUtils.validHostname(s), "Valid host name: " + s);
        }

        for (String s : straInvalidHosts) {
            assertTrue(!ValidationUtils.validHostname(s), "Invalid host name: " + s);
        }
    }

    @Test
    public void testTrimmingWhitespaces() {
        assertPatternMatches("Valid string (no trimming whitespaces): ", ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN, "", "aoeu", "a o e u ř", "%2123 o ^ ooe#", "a");
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
    public void validIpv6() {
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("a:b:c:d:e:f:a:b"));
        assertTrue(ValidationUtils.isValidIpv6("abcd:1bcd:a2cd:ab3d:abc4:0000:ffff:0f0f"));

        assertTrue(ValidationUtils.isValidIpv6("1::"));
        assertTrue(ValidationUtils.isValidIpv6("1::8"));
        assertTrue(ValidationUtils.isValidIpv6("1::7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1::6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1::5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1::4:5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1::3:4:5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("::2:3:4:5:6:7:8"));

        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5:6:7::"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5:6::8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5::8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4::8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3::8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2::8"));
        assertTrue(ValidationUtils.isValidIpv6("1::8"));
        assertTrue(ValidationUtils.isValidIpv6("::8"));

        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5:6::8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4:5::7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3:4::6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2:3::5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1:2::4:5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("1::3:4:5:6:7:8"));
        assertTrue(ValidationUtils.isValidIpv6("::2:3:4:5:6:7:8"));

        assertFalse(ValidationUtils.isValidIpv6(null));
        assertFalse(ValidationUtils.isValidIpv6(""));
        assertFalse(ValidationUtils.isValidIpv6("1.2.3.4"));
        assertFalse(ValidationUtils.isValidIpv6("fe80::7:8%eth0"));
        assertFalse(ValidationUtils.isValidIpv6("fe80::7:8%1"));
        assertFalse(ValidationUtils.isValidIpv6("abcd"));
        assertFalse(ValidationUtils.isValidIpv6("abcd "));
        assertFalse(ValidationUtils.isValidIpv6("abcd e"));
    }

    @Test
    public void validIpv4() {
        assertTrue(ValidationUtils.isValidIpv4("0.0.0.0"));
        assertTrue(ValidationUtils.isValidIpv4("255.255.255.255"));

        assertFalse(ValidationUtils.isValidIpv4("192.168.122.1/24"));

        assertFalse(ValidationUtils.isValidIpv4("0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("255"));
        assertFalse(ValidationUtils.isValidIpv4("255.255"));
        assertFalse(ValidationUtils.isValidIpv4("255.255.255"));

        assertFalse(ValidationUtils.isValidIpv4("256.255.255.255"));
        assertFalse(ValidationUtils.isValidIpv4("255.256.255.255"));
        assertFalse(ValidationUtils.isValidIpv4("255.255.256.255"));
        assertFalse(ValidationUtils.isValidIpv4("255.255.255.256"));

        assertFalse(ValidationUtils.isValidIpv4("256.0.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.256.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.256.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.0.256"));

        assertFalse(ValidationUtils.isValidIpv4("0001.0.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0001.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.0001.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.0.0001"));

        assertFalse(ValidationUtils.isValidIpv4("1 .0.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0. 1.0.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0. 1.0"));
        assertFalse(ValidationUtils.isValidIpv4("0.0.0. 1"));

        assertFalse(ValidationUtils.isValidIpv4("a.1.1.1"));
        assertFalse(ValidationUtils.isValidIpv4("1.a.1.1"));
        assertFalse(ValidationUtils.isValidIpv4("1.1.a.1"));
        assertFalse(ValidationUtils.isValidIpv4("1.1.1.a"));
    }

    @Test
    public void testValidIsoPath() {
        assertPatternMatches("Valid isoPath: ", ValidationUtils.ISO_SUFFIX_PATTERN, "", "foo.iso", "RHEVM-123456-tools.iso");
        assertPatternDoesNotMatch("Invalid isoPath: ", ValidationUtils.ISO_SUFFIX_PATTERN, "x", "sysprep.vfd", "disk.ISO");
    }

    private void assertPatternMatches(String message, String pattern, String... validStrings) {
        for (String s : validStrings) {
            assertTrue(Pattern.matches(pattern, s), message + s);
        }
    }

    private void assertPatternDoesNotMatch(String message, String pattern, String... invalidStrings) {
        for (String s : invalidStrings) {
            assertTrue(!Pattern.matches(pattern, s), message + s);
        }
    }
}
