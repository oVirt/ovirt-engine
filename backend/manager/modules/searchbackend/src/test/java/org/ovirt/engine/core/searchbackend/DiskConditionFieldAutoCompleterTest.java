package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DiskConditionFieldAutoCompleterTest {

    @Test
    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new DiskConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("FORMAT", "RAW"));
    }
}
