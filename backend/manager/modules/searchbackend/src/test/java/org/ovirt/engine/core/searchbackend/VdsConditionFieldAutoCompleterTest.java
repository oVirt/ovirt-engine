package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Use this to test the BigDecimal delegate
 *
 *
 */
public class VdsConditionFieldAutoCompleterTest {

    private VdsConditionFieldAutoCompleter underTest;

    @Before
    public void setup() {
        underTest = new VdsConditionFieldAutoCompleter();
    }
    @Test
    public void testValidate() {
        assertTrue("1", underTest.validateFieldValue("LOAD", "1"));
        assertTrue("123", underTest.validateFieldValue("LOAD", "123"));
        assertTrue("123.456", underTest.validateFieldValue("LOAD", "123.456"));
        assertFalse("JarJar", underTest.validateFieldValue("LOAD", "JarJar"));
    }

    @Test
    public void validHaScoreTerm() {
        assertTrue(underTest.validateFieldValue("ha_score", "0"));
        assertTrue(underTest.validateFieldValue("ha_score", "3400"));
    }

}
