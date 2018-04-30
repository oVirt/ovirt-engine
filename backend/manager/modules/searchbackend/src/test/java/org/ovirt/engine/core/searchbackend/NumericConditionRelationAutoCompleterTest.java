package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class NumericConditionRelationAutoCompleterTest {

    @Test
    public void testValue() {
        List<String> comps = Arrays.asList(NumericConditionRelationAutoCompleter.INSTANCE.getCompletion("<"));
        assertTrue(comps.contains("<"), "<");
        assertTrue(comps.contains("<="), "<=");
        assertFalse(comps.contains("="), "=");
    }
}
