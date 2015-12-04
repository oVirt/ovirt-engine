package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RegexTest {

    @Test
    public void testSimpleMatches() {
        Regex regex = new Regex("[0-9]");
        assertTrue("A number should match", regex.isMatch("1"));
        assertFalse("A letter should not match", regex.isMatch("a"));
    }

    @Test
    public void testIsMatch() {
        assertTrue("A number should match", Regex.isMatch("1", "[0-9]"));
        assertFalse("A letter should not match", Regex.isMatch("a", "[0-9]"));
    }

    @Test
    public void testIgnoreCaseOff() {
        Regex regex = new Regex("[A-Z]");
        assertTrue("A cap should match", regex.isMatch("K"));
        assertFalse("A lowercase should not match", regex.isMatch("k"));
    }

    @Test
    public void testIgnoreCaseOn() {
        Regex regex = new Regex("[A-Z]", RegexOptions.IgnoreCase);
        assertTrue("A cap should match", regex.isMatch("K"));
        assertTrue("A lowercase should  match", regex.isMatch("k"));
    }

    @Test
    public void testGroups() {
        Match match = Regex.match("ABC,123", "([A-Z]+),([0-9]+)");
        assertEquals("First Group", "ABC", match.groups().get(0).getValue());
        assertEquals("Second Group", "123", match.groups().get(1).getValue());
    }
}
