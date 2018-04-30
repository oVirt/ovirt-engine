package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Use this to test the TimeSpan validators
 *
 *
 */
public class VmConditionFieldAutoCompleterTest {

    @Test
    public void testValidate() {
        VmConditionFieldAutoCompleter comp = new VmConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("UPTIME", "-02:03:04.05"), "-02:03:04.05");
        assertTrue(comp.validateFieldValue("UPTIME", "1"), "1");
        assertFalse(comp.validateFieldValue("UPTIME", "FerEver"), "Fer Ever");
    }

    @Test
    public void testDescriptionField() {
        VmConditionFieldAutoCompleter comp = new VmConditionFieldAutoCompleter();
        assertTrue(comp.validationDict.containsKey("DESCRIPTION"));
        assertTrue(comp.validateFieldValue("DESCRIPTION", "bar"));
    }

}
