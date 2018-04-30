package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RegexTest {

    @Test
    public void testSimpleMatches() {
        Regex regex = new Regex("[0-9]");
        assertTrue(regex.isMatch("1"), "A number should match");
        assertFalse(regex.isMatch("a"), "A letter should not match");
    }

    @Test
    public void testIsMatch() {
        assertTrue(Regex.isMatch("1", "[0-9]"), "A number should match");
        assertFalse(Regex.isMatch("a", "[0-9]"), "A letter should not match");
    }

    @Test
    public void testIgnoreCaseOff() {
        Regex regex = new Regex("[A-Z]");
        assertTrue(regex.isMatch("K"), "A cap should match");
        assertFalse(regex.isMatch("k"), "A lowercase should not match");
    }

    @Test
    public void testIgnoreCaseOn() {
        Regex regex = new Regex("[A-Z]", RegexOptions.IgnoreCase);
        assertTrue(regex.isMatch("K"), "A cap should match");
        assertTrue(regex.isMatch("k"), "A lowercase should  match");
    }

    @Test
    public void testGroups() {
        Match match = Regex.match("ABC,123", "([A-Z]+),([0-9]+)");
        assertEquals("ABC", match.groups().get(0).getValue(), "First Group");
        assertEquals("123", match.groups().get(1).getValue(), "Second Group");
    }
}
