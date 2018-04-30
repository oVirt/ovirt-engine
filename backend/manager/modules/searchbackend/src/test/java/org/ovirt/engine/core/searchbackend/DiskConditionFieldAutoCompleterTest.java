package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DiskConditionFieldAutoCompleterTest {

    @Test
    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new DiskConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("FORMAT", "RAW"));
    }
}
