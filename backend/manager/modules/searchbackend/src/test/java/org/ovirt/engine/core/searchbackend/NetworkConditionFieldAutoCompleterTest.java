package org.ovirt.engine.core.searchbackend;

import junit.framework.TestCase;

public class NetworkConditionFieldAutoCompleterTest extends TestCase {

    public void testValidateFieldValueWithEnum() {
        IConditionFieldAutoCompleter comp = new NetworkConditionFieldAutoCompleter();
        assertTrue(comp.validateFieldValue("NAME", "ABC"));
    }
}
