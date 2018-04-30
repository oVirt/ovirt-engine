package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BaseConditionFieldAutoCompleterTest {
    @Test
    public void validTimeSpan() {
        assertFalse(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, ""));
        assertFalse(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, "not valid"));
        assertTrue(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, "-1.02:03:04.05"));
    }

    @Test
    public void validInteger() {
        assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, ""));
        assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "not valid integer"));
        assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "$%@^%"));
        assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1$"));
        assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1.1"));
        assertTrue(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1"));
        assertTrue(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "-1"));
    }

    @Test
    public void validDecimal() {
        assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, ""));
        assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "not valid integer"));
        assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "$%@^%"));
        assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1$"));
        assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1.1"));
        assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1"));
        assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "-1"));
        assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "-1.1"));
    }

}
