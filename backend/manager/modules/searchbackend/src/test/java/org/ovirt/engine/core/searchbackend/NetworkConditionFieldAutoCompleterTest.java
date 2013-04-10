package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NetworkConditionFieldAutoCompleterTest {

    @Test
    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new NetworkConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("NAME", "ABC"));
    }
}
