package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Use this to test the TimeSpan validators
 *
 *
 */
public class VmConditionFieldAutoCompleterTest {

    @Test
    public void testValidate() {
        VmConditionFieldAutoCompleter comp = new VmConditionFieldAutoCompleter();
        assertTrue("-02:03:04.05", comp.validateFieldValue("UPTIME", "-02:03:04.05"));
        assertTrue("1", comp.validateFieldValue("UPTIME", "1"));
        assertFalse("Fer Ever", comp.validateFieldValue("UPTIME", "FerEver"));
    }

    @Test
    public void testDescriptionField() {
        VmConditionFieldAutoCompleter comp = new VmConditionFieldAutoCompleter();
        assertTrue(comp.validationDict.containsKey("DESCRIPTION"));
        assertTrue(comp.validateFieldValue("DESCRIPTION", "bar"));
    }

}
