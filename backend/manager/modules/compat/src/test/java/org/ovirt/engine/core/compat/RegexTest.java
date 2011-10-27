package org.ovirt.engine.core.compat;

import junit.framework.TestCase;

public class RegexTest extends TestCase {
    public void testSimpleMatches() {
        Regex regex = new Regex("[0-9]");
        assertTrue("A number should match", regex.IsMatch("1"));
        assertFalse("A letter should not match", regex.IsMatch("a"));
    }

    public void testIsMatch() {
        assertTrue("A number should match", Regex.IsMatch("1", "[0-9]"));
        assertFalse("A letter should not match", Regex.IsMatch("a", "[0-9]"));
    }

    public void testIgnoreCaseOff() {
        Regex regex = new Regex("[A-Z]");
        assertTrue("A cap should match", regex.IsMatch("K"));
        assertFalse("A lowercase should not match", regex.IsMatch("k"));
    }

    public void testIgnoreCaseOn() {
        Regex regex = new Regex("[A-Z]", RegexOptions.IgnoreCase);
        assertTrue("A cap should match", regex.IsMatch("K"));
        assertTrue("A lowercase should  match", regex.IsMatch("k"));
    }

    public void testGroups() {
        Match match = Regex.Match("ABC,123", "([A-Z]+),([0-9]+)");
        assertEquals("First Group", "ABC", match.Groups().get(0).getValue());
        assertEquals("Second Group", "123", match.Groups().get(1).getValue());
    }
}
