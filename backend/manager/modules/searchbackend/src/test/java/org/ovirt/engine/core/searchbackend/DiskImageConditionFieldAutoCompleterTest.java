package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

public class DiskImageConditionFieldAutoCompleterTest extends TestCase {

    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new DiskImageConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("FORMAT", "RAW"));
    }
}
