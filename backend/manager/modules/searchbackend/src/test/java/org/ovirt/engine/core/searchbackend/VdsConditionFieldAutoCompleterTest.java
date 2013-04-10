package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Use this to test the BigDecimal delegate
 *
 *
 */
public class VdsConditionFieldAutoCompleterTest {

    @Test
    public void testValidate() {
        VdsConditionFieldAutoCompleter comp = new VdsConditionFieldAutoCompleter();
        assertTrue("1", comp.validateFieldValue("LOAD", "1"));
        assertTrue("123", comp.validateFieldValue("LOAD", "123"));
        assertTrue("123.456", comp.validateFieldValue("LOAD", "123.456"));
        assertFalse("JarJar", comp.validateFieldValue("LOAD", "JarJar"));
    }

}
