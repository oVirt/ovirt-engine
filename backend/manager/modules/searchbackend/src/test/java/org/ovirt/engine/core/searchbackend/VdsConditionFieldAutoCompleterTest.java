package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

/**
 * Use this to test the BigDecimal delegate
 *
 *
 */
public class VdsConditionFieldAutoCompleterTest extends TestCase {

    public void testValidate() {
        VdsConditionFieldAutoCompleter comp = new VdsConditionFieldAutoCompleter();
        assertTrue("1", comp.validateFieldValue("LOAD", "1"));
        assertTrue("123", comp.validateFieldValue("LOAD", "123"));
        assertTrue("123.456", comp.validateFieldValue("LOAD", "123.456"));
        assertFalse("JarJar", comp.validateFieldValue("LOAD", "JarJar"));
    }

}
