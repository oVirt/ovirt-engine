package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Use this to test the BigDecimal delegate
 *
 *
 */
public class VdsConditionFieldAutoCompleterTest {

    private VdsConditionFieldAutoCompleter underTest;

    @BeforeEach
    public void setup() {
        underTest = new VdsConditionFieldAutoCompleter();
    }
    @Test
    public void testValidate() {
        assertTrue(underTest.validateFieldValue("LOAD", "1"), "1");
        assertTrue(underTest.validateFieldValue("LOAD", "123"), "123");
        assertTrue(underTest.validateFieldValue("LOAD", "123.456"), "123.456");
        assertFalse(underTest.validateFieldValue("LOAD", "JarJar"), "JarJar");
    }

    @Test
    public void validHaScoreTerm() {
        assertTrue(underTest.validateFieldValue("ha_score", "0"));
        assertTrue(underTest.validateFieldValue("ha_score", "3400"));
    }

}
