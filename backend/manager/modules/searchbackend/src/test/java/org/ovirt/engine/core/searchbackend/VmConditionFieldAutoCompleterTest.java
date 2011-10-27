package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

/**
 * Use this to test the TimeSpan validators
 *
 *
 */
public class VmConditionFieldAutoCompleterTest extends TestCase {

    public void testValidate() {
        VmConditionFieldAutoCompleter comp = new VmConditionFieldAutoCompleter();
        assertTrue("-02:03:04.05", comp.validateFieldValue("UPTIME", "-02:03:04.05"));
        assertTrue("1", comp.validateFieldValue("UPTIME", "1"));
        assertFalse("Fer Ever", comp.validateFieldValue("UPTIME", "FerEver"));
    }

}
