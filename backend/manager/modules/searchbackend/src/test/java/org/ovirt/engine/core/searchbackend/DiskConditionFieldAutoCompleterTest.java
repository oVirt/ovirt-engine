package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

public class DiskConditionFieldAutoCompleterTest extends TestCase {

    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new DiskConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("FORMAT", "RAW"));
    }
}
