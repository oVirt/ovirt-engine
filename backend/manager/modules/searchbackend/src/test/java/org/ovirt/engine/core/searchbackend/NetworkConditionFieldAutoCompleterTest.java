package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NetworkConditionFieldAutoCompleterTest {

    @Test
    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new NetworkConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("NAME", "ABC"));
    }
}
